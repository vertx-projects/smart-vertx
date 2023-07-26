package com.smart.vertx.verticle;

import com.google.common.collect.Lists;
import com.smart.vertx.VertxClusterProperties;
import com.smart.vertx.VertxProperties;
import com.smart.vertx.annotation.RequestMapping;
import com.smart.vertx.annotation.Sign;
import com.smart.vertx.core.sign.SignHandler;
import com.smart.vertx.entity.ResponseResult;
import com.smart.vertx.core.VertxFilterSpanDecorator;
import com.smart.vertx.util.SpringUtils;
import com.smart.vertx.verticle.cluster.ClusterManagerStrategy;
import com.smart.vertx.verticle.handler.CustomErrorHandler;
import com.smart.vertx.verticle.inspection.CheckParams;
import com.smart.vertx.verticle.inspection.IProcessorChain;
import com.smart.vertx.verticle.interceptor.HandlerInterceptor;
import com.smart.vertx.verticle.request.IParamStrategy;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.tracing.TracingPolicy;

import java.util.Objects;

import io.vertx.rxjava3.circuitbreaker.HystrixMetricHandler;
import io.vertx.rxjava3.core.eventbus.Message;
import io.vertx.rxjava3.ext.web.Route;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.handler.*;
import io.vertx.rxjava3.micrometer.PrometheusScrapingHandler;
import io.vertx.rxjava3.sqlclient.Tuple;
import io.vertx.tracing.opentracing.OpenTracingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc verticle 启动类
 */
@Slf4j
public class HttpServerVerticle extends SuperVerticle {
    @Override
    public Completable rxStart() {
        Integer port = springContext.getEnvironment().getProperty("server.port", Integer.class, getPort());
        VertxProperties props = springContext.getBean(VertxProperties.class);
        // 构造server
        Router router = Router.router(vertx);
        // 构造handler
        log.info("verticle is starting......");
        return springContext.getBean(VerticleHandler.class)
                .buildData(props.getPackageName())
                .compose(s -> buildHandler(router, s, props))
                .flatMap(s -> vertx.createHttpServer(new HttpServerOptions()
                                .setTracingPolicy(TracingPolicy.ALWAYS)).requestHandler(router)
                        .exceptionHandler(throwable -> log.error("verticle invoke error {}.", throwable.getMessage(), throwable))
                        .rxListen(port).doOnSuccess(p -> log.info("verticle is started , port:{}", p.actualPort())))
                .flatMap(server -> startWebClient(props, server))
                .map(server -> startEventBus(props))
                .onErrorComplete(throwable -> {
                    log.error("failed to start verticle {}.", throwable.getMessage(), throwable);
                    return true;
                }).ignoreElement();
    }

    /**
     * 添加请求处理器
     */
    private Single<Boolean> buildHandler(Router router, Single<Map<String, List<Tuple>>> map, VertxProperties props) {
        return map.map(s -> {
            s.forEach((k, v) -> {
                for (Tuple tuple : v) {
                    RequestMapping mapping = tuple.get(RequestMapping.class, 0);
                    String routePath = String.format("/%s%s%s", props.getApiVersion(), k, mapping.value());
                    Class<?> clazz = tuple.get(Class.class, 2);
                    log.info("mapping {} {} to {}", mapping.method(), routePath, clazz);
                    HttpMethod method = HttpMethod.valueOf(mapping.method().name());
                    processHandler(router.route(method, routePath)
                            .failureHandler(ErrorHandler.newInstance(new CustomErrorHandler(vertx.getDelegate())))
                            .handler(ResponseTimeHandler.create())
                            .handler(ResponseContentTypeHandler.create())
                            .handler(CorsHandler.create()
                                    .maxAgeSeconds(3600)
                                    .allowCredentials(true)
                                    .allowedMethod(HttpMethod.GET)
                                    .allowedMethod(HttpMethod.POST)
                                    .allowedMethod(HttpMethod.PUT)
                                    .allowedMethod(HttpMethod.DELETE)
                                    .allowedMethod(HttpMethod.OPTIONS)
                                    .allowedMethod(HttpMethod.TRACE)
                                    .allowedMethod(HttpMethod.CONNECT)
                                    .allowedMethod(HttpMethod.PATCH)
                                    .allowedMethod(HttpMethod.HEAD))
                            .handler(TimeoutHandler.create(props.getMaxEventLoopExecuteTime(),
                                    HttpResponseStatus.GATEWAY_TIMEOUT.code())), tuple, method);
                }
            });
            return Objects.nonNull(SpringUtils.getBean(springContext, VertxClusterProperties.Maker.class));
        }).doOnSuccess(s -> {
            if (props.isMetrics()) {
                router.route("/vertx/metrics").handler(PrometheusScrapingHandler.create());
                log.info("mapping GET metrics {} to vertx success", "/metrics");
            }
            router.get("/hystrix-metrics").handler(HystrixMetricHandler.create(vertx));
            log.info("cluster manager start init.");
            vertx.executeBlocking(t -> {
                while (true) {
                    if (ClusterManagerStrategy.maps.size() == 2) {
                        log.info("cluster manager init success.");
                        if (s) ClusterManagerStrategy.get(props.getClusterType()).startClusterManager(router, props);
                        break;
                    }
                }
            }).subscribe();
        });
    }

    private void processHandler(Route route, Tuple tuple, HttpMethod httpMethod) {
        if (!httpMethod.equals(HttpMethod.GET)) {
            route.handler(BodyHandler.create());
        }
        Method method = tuple.get(Method.class, 1);
        RequestMapping mapping = tuple.get(RequestMapping.class, 0);
        Class<?> clazz = tuple.get(Class.class, 2);
        Object instance = springContext.getBean(clazz);
        if (mapping.blocked()) {
            route.blockingHandler(s -> handlerInvoke(method, instance, s), mapping.blockOrdered())
                    .produces(mapping.contentType());
            return;
        }
        route.handler(s -> {
            //拦截器实现
            Collection<HandlerInterceptor> interceptors = SpringUtils.getBeans(springContext, HandlerInterceptor.class);
            if (!CollectionUtils.isEmpty(interceptors)) {
                for (HandlerInterceptor interceptor : interceptors) {
                    if (Pattern.matches(interceptor.pattern(), route.getPath())
                            && interceptor.preHandler(s, instance)) {
                        handlerInvoke(method, instance, s);
                        interceptor.postHandler(s, instance);
                        //同一个路由地址只能匹配一个拦截器，故该拦截器只执行一次
                        break;
                    }

                }

            } else {
                handlerInvoke(method, instance, s);
            }
        }).produces(mapping.contentType());
    }

    public void handlerInvoke(Method method, Object instance, RoutingContext s) {
        method.setAccessible(true);
        IProcessorChain.PROCESSOR.get().handler(CheckParams.builder().s(s).method(method).build())
                .filter(f -> f).switchIfEmpty(Single.never())
                .flatMap(t -> Observable.fromArray(method.getParameters())
                        .map(parameter -> Tuple.of(IParamStrategy.get(parameter), parameter))
                        .reduce(Lists.newArrayList(), (items, tuple) -> {
                            IParamStrategy strategy = tuple.get(IParamStrategy.class, 0);
                            if (Objects.isNull(strategy)) {
                                items.add(null);
                            } else {
                                strategy.init(items, tuple.get(Parameter.class, 1), s);
                            }
                            return items;
                        }).map(y -> y.toArray(new Object[]{})))
                .flatMap(items -> {
                    //复杂验签认证添加
                    Sign sign = method.getAnnotation(Sign.class);
                    SignHandler signHandler = springContext.getBean(SignHandler.class);
                    return Objects.isNull(sign)
                            ? Single.just(items)
                            : signHandler.invokeSelf(method.getParameters(), items).map(y -> items);
                })
                .flatMap(items -> {
                    Object o = method.invoke(instance, items);
                    if (o instanceof SingleSource) {
                        return (SingleSource<?>) o;
                    } else if (o instanceof CompletableSource) {
                        return Single.just("completable");
                    } else if (o instanceof Future<?>) {
                        Future<?> future = (Future<?>) o;
                        return Single.fromCompletionStage(future.toCompletionStage());
                    } else {
                        return Single.just(o);
                    }
                }).map(vo -> {
                    if (vo instanceof Message) {
                        return s.response().end((ResponseResult.success(((Message<?>) vo).body())).toString());
                    } else if (vo instanceof Future<?>) {
                        ((Future<?>) vo).map(c -> s.response().end(ResponseResult.success(c).toString()))
                                .recover(throwable -> {
                                    s.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), throwable);
                                    log.error("invoke future handler error.", throwable);
                                    return Future.failedFuture(throwable.getMessage());
                                })
                                .onComplete(p -> {
                                });
                        return Completable.complete();
                    } else {
                        return s.response().end(ResponseResult.success(vo).toString());
                    }
                }).onErrorComplete(throwable -> startException(s, throwable))
                .doFinally(() -> VertxFilterSpanDecorator.STANDARD_TAGS.onResponse(s.request(), s.response(), OpenTracingUtil.getSpan()))
                .subscribe();
    }
}
