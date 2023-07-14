package com.smart.vertx.verticle;

import com.google.common.collect.Lists;
import com.smart.vertx.GuiceContext;
import com.smart.vertx.core.VertxProperties;
import com.smart.vertx.annotation.RequestMapping;
import com.smart.vertx.entity.ResponseResult;
import com.smart.vertx.exception.VertxDeploymentException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.eventbus.Message;
import io.vertx.rxjava3.ext.web.Route;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.handler.*;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc verticle 启动类
 */
@Slf4j
public class HttpServerVerticle extends SuperVerticle {
    @Override
    public Completable rxStart() {
        VertxProperties props = GuiceContext.getBean(VertxProperties.class);
        int port = props.getPort();
        // 构造server
        Router router = Router.router(vertx);
        // 构造handler
        log.info("verticle is starting......");

        return Single.defer(() -> Single.just(GuiceContext.getBean(VerticleHandler.class)))
                .map(s -> s.buildHandler(props.getPackageName()))
                .map(s -> {
                    addHandlers(router, s, props);
                    return true;
                }).flatMap(s -> vertx.createHttpServer().requestHandler(router)
                        .exceptionHandler(throwable -> log.error("verticle invoke error,{}", throwable.getLocalizedMessage(), throwable)).rxListen(port).doOnSuccess(p -> {
                            log.info("verticle is started , port:{}", p.actualPort());
                        }))
                .flatMap(server -> buildWebClient(props, server))
                .map(server -> {
                    buildEventBus(props);
                    return true;
                }).onErrorComplete(throwable -> {
                    log.error("failed to start verticle,{}", throwable.getLocalizedMessage(), throwable);
                    throw new VertxDeploymentException("failed to start verticle", throwable);
                }).ignoreElement();
    }


    /**
     * 添加请求处理器
     */
    private void addHandlers(Router router, Map<String, List<Tuple>> map, VertxProperties props) {
        map.forEach((k, v) -> {
            for (Tuple tuple : v) {
                RequestMapping mapping = tuple.get(RequestMapping.class, 0);
                String routePath = String.format("/%s%s%s", props.getApiVersion(), k, mapping.value());
                Class<?> clazz = tuple.get(Class.class, 2);
                log.info("mapping {} {} to {}", mapping.method(), routePath, clazz);
                HttpMethod method = HttpMethod.valueOf(mapping.method().name());
                processHandler(router.route(method, routePath)
                        .failureHandler(ErrorHandler.create(vertx))
                        .handler(ResponseTimeHandler.create())
                        .handler(ResponseContentTypeHandler.create())
                        .handler(TimeoutHandler.create(props.getMaxEventLoopExecuteTime(),
                                HttpResponseStatus.GATEWAY_TIMEOUT.code())), tuple, method);

            }
        });
    }

    private void processHandler(Route route, Tuple tuple, HttpMethod httpMethod) {
        if (!httpMethod.equals(HttpMethod.GET)) {
            route.handler(BodyHandler.create());
        }
        RequestMapping mapping = tuple.get(RequestMapping.class, 0);
        Method method = tuple.get(Method.class, 1);
        Class<?> clazz = tuple.get(Class.class, 2);
        Object instance = GuiceContext.getBean(clazz);
        if (mapping.blocked()) {
            route.blockingHandler(s -> {
                handlerInvoke(method, instance, s);
            }, mapping.blockOrdered()).produces(mapping.contentType());
            return;
        }
        route.handler(s -> {
            handlerInvoke(method, instance, s);
        }).produces(mapping.contentType());
    }

    private void handlerInvoke(Method method, Object instance, RoutingContext s) {
        HttpVerticleFactory factory = GuiceContext.getBean(HttpVerticleFactory.class);
        Single.defer(() -> {
            List<Object> items = Lists.newArrayList();
            //优化反射access查询
            method.setAccessible(true);
            for (Parameter parameter : method.getParameters()) {
                IParamStrategy strategy = factory.get(parameter);
                if (Objects.isNull(strategy)) {
                    items.add(null);
                }
                Single<Object> single = strategy.init(items, parameter, s);
                if (!single.blockingGet().getClass().equals(Boolean.class)) {
                    return single;
                }
            }
            Object o = method.invoke(instance, items.toArray(new Object[]{}));
            if (o instanceof SingleSource) {
                return (SingleSource<?>) o;
            } else {
                return Single.just("completable");
            }
        }).doOnSuccess(vo -> {
            s.response().setStatusCode(HttpResponseStatus.OK.code());
            if (vo instanceof Message<?>) {
                s.response().end((ResponseResult.success(((Message<?>) vo).body())).toString());
            } else {
                s.response().end(ResponseResult.success(vo).toString());
            }

        }).onErrorComplete(throwable -> {
            log.error("invoke handler error.", throwable);
            if (throwable instanceof InvocationTargetException) {
                s.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), ((InvocationTargetException) throwable).getTargetException());
            } else {
                s.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), throwable);
            }
            return true;
        }).subscribe();
    }
}
