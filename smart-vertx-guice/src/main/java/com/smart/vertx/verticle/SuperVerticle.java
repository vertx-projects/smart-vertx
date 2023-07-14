package com.smart.vertx.verticle;

import com.google.common.collect.Lists;
import com.smart.vertx.GuiceContext;
import com.smart.vertx.core.VertxProperties;
import com.smart.vertx.annotation.EventBus;
import com.smart.vertx.enums.CommonConstEnums;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.http.HttpServer;
import io.vertx.rxjava3.servicediscovery.ServiceDiscovery;
import io.vertx.rxjava3.servicediscovery.types.EventBusService;
import io.vertx.rxjava3.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author peng.bo
 * @date 2022/5/25 16:11
 * @desc
 */
@Slf4j
public abstract class SuperVerticle extends AbstractVerticle {
    public static List<Class<?>> classes = Lists.newArrayList();

    protected int getPort() {
        return 8080;
    }

    protected Single<Record> buildWebClient(VertxProperties props, HttpServer server) {
        ServiceDiscovery discovery = GuiceContext.getBean(ServiceDiscovery.class);
        // Once the HTTP server is started (we are ready to serve)
        // we publish the service.
        Record record = HttpEndpoint.createRecord(props.getServiceName(), props.getHost(), server.actualPort(), props.getServiceName() + "/" + props.getApiVersion() + "/");
        // We publish the service
        return discovery.rxPublish(record).map(s -> {
            log.info("vertx webclient discovery is started , record:{}", s);
            return s;
        });
    }

    protected void buildEventBus(VertxProperties props) {
        ServiceDiscovery discovery = GuiceContext.getBean(ServiceDiscovery.class);
        // Once the HTTP server is started (we are ready to serve)
        // we publish the service.
        for (Class<?> aClass : classes) {
            try {
                GuiceContext.getBean(aClass);
            } catch (Exception e) {
                log.debug("this service ignore eventbus discovery,{}", props.getServiceName());
                continue;
            }
            EventBus eventBus = aClass.getAnnotation(EventBus.class);
            Record record = EventBusService.createRecord(props.getServiceName().concat(CommonConstEnums._eventbus.name()), // 服务名称
                    eventBus.address(), // 服务地址,
                    aClass.getName() // 接口类
            );
            // We publish the service
            discovery.publish(record).map(s -> {
                log.info("vertx eventbus discovery is started , record:{}", s);
                return s;
            }).subscribe();
        }
    }
}
