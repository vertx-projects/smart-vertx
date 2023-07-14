package com.smart.vertx.verticle;

import com.google.common.collect.Sets;
import com.smart.vertx.VertxProperties;
import com.smart.vertx.annotation.EventBus;
import com.smart.vertx.core.VertxFilterSpanDecorator;
import com.smart.vertx.enums.CommonConstEnums;
import com.smart.vertx.util.SpringUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.http.HttpServer;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.servicediscovery.ServiceDiscovery;
import io.vertx.rxjava3.servicediscovery.types.EventBusService;
import io.vertx.rxjava3.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;
import io.vertx.tracing.opentracing.OpenTracingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Set;

/**
 * @author peng.bo
 * @date 2022/5/25 16:11
 * @desc
 */
@Slf4j
public abstract class SuperVerticle extends AbstractVerticle {
    public static ApplicationContext springContext;
    public static Set<Class<?>> classes = Sets.newConcurrentHashSet();

    protected int getPort() {
        return 8080;
    }

    protected Single<Record> startWebClient(VertxProperties props, HttpServer server) {
        ServiceDiscovery discovery = springContext.getBean(ServiceDiscovery.class);
        // Once the HTTP server is started (we are ready to serve)
        // we publish the service.
        Record record = HttpEndpoint.createRecord(props.getServiceName(), props.getHost(), server.actualPort(), props.getApiVersion() + "/");
        // We publish the service
        return discovery.rxPublish(record).map(s -> {
            log.info("vertx webclient discovery is started , record:{}", s);
            return s;
        });
    }

    protected boolean startEventBus(VertxProperties props) {
        ServiceDiscovery discovery = springContext.getBean(ServiceDiscovery.class);
        // Once the EventBus server is started (we are ready to serve)
        // we publish the service.
        for (Class<?> aClass : classes) {
            if (Objects.isNull(SpringUtils.getBean(springContext, aClass))) {
                log.debug("this service ignore eventbus discovery,{}", props.getServiceName());
                continue;
            }
            EventBus eventBus = aClass.getAnnotation(EventBus.class);
            Record record = EventBusService.createRecord(eventBus.serviceName().concat(CommonConstEnums._eventbus.name()), // 服务名称
                    eventBus.address(), // 服务地址,
                    aClass.getName() // 接口类
            );
            // We publish the service
            discovery.publish(record).map(s -> {
                log.info("vertx eventbus discovery is started , record:{}", s);
                return s;
            }).subscribe();
        }
        return true;
    }

    protected boolean startException(RoutingContext s, Throwable throwable) {
        if (throwable instanceof InvocationTargetException) {
            log.error("invoke handler error.", ((InvocationTargetException) throwable).getTargetException());
            s.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), ((InvocationTargetException) throwable).getTargetException());
        } else {
            log.error("invoke handler error.", throwable);
            s.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), throwable);
        }
        VertxFilterSpanDecorator.STANDARD_TAGS.onError(s.request(), s.response(), throwable, OpenTracingUtil.getSpan());
        return true;
    }
}