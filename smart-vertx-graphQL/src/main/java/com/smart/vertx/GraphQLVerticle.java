package com.smart.vertx;

import com.smart.vertx.exception.VertxDeploymentException;
import com.smart.vertx.util.SpringUtils;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import io.reactivex.rxjava3.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.handler.graphql.instrumentation.VertxFutureAdapter;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.Route;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;
import io.vertx.rxjava3.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.rxjava3.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.rxjava3.ext.web.handler.graphql.ws.GraphQLWSHandler;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderFactory;
import org.dataloader.DataLoaderRegistry;
import org.springframework.context.ApplicationContext;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx
 * @date 2023/7/19 17:56
 */
@Slf4j
public class GraphQLVerticle extends AbstractVerticle {
    private VertxUnaryOperator vertxUnary;
    private final ApplicationContext springContext;

    public GraphQLVerticle(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    @Override
    public Completable rxStart() {
        VertxUnaryOperator operator = SpringUtils.getBean(springContext, VertxUnaryOperator.class);
        if (Objects.isNull(operator)) {
            throw new VertxDeploymentException("There can be only one bean implementation of the [VertxUnaryOperator] interface.");
        }
        this.vertxUnary = operator;
        Integer port = springContext.getEnvironment().getProperty("server.port", Integer.class, 8080);
        Router router = Router.router(vertx);
        HttpServerOptions httpServerOptions = new HttpServerOptions();
        //start GraphQL
        router.route("/graphiql/*").handler(GraphiQLHandler.create());
        Route route = router.route("/graphql").handler(BodyHandler.create());
        if (vertxUnary.subscription()) {
            httpServerOptions.addWebSocketSubProtocol("graphql-transport-ws");
            route.handler(GraphQLWSHandler.create(createGraphQL()));
        }
        GraphQLHandler graphQLHandler = GraphQLHandler.create(createGraphQL());
        //batch loader check
        if (!operator.dataLoader().isEmpty()) {
            operator.dataLoader().forEach((k, v) -> graphQLHandler.beforeExecute(builderWithContext -> {
                DataLoader<String, ?> dataLoader = DataLoaderFactory.newDataLoader(v);
                DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry().register(k, dataLoader);
                builderWithContext.builder().dataLoaderRegistry(dataLoaderRegistry);
                log.info("dataloader [{}] register success.", k);
            }));
        }
        route.handler(graphQLHandler);
        log.info("graphQL server verticle is starting......");
        return vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .exceptionHandler(throwable -> log.error("graphQL server invoke error,{}", throwable.getLocalizedMessage(), throwable))
                .listen(port).doOnSuccess(p -> log.info("graphQL server verticle is started , port:{}", p.actualPort()))
                .onErrorComplete(throwable -> {
                    log.error("failed to start graphQL server verticle,{}", throwable.getLocalizedMessage(), throwable);
                    throw new VertxDeploymentException("failed to start graphQL server verticle", throwable);
                }).ignoreElement();
    }

    private GraphQL createGraphQL() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(vertxUnary.graphSDLPath());

        if (Objects.isNull(stream)) {
            throw new VertxDeploymentException("load [" + vertxUnary.graphSDLPath() + "] resource fail.");
        }
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(stream);
        //typeDefinitionRegistry.merge(schemaParser.parse(stream));

        //Subscription Query
        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();
        vertxUnary.operator().forEach(builder::type);
        RuntimeWiring runtimeWiring = builder.build();
        SchemaGenerator schemaGenerator = new SchemaGenerator();

        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).instrumentation(VertxFutureAdapter.create()).build();
    }
}
