package com.smart.vertx.core;

import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.servicediscovery.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.monitor
 * @date 2022/6/29 13:50
 */
@Slf4j
@Component
public class MonitorHandler implements DisposableBean, ApplicationListener<ContextClosedEvent> {
    @Resource
    private Vertx vertx;

    @Resource
    ServiceDiscovery serviceDiscovery;

    @PostConstruct
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            serviceDiscovery.close();
            log.info("start stop vertx");
            vertx.close().doOnError(throwable -> {
                log.debug("##############vertx stop error.{}###########", throwable.toString());
            }).doOnComplete(() -> {
                log.debug("##############vertx stop success.###########");
            }).doFinally(() -> {
                log.debug("##############vertx stop over.###########");
            }).subscribe();
        }));
    }

    @Override
    public void destroy() {
        serviceDiscovery.close();
        vertx.close().doOnError(throwable -> {
            log.debug("##############vertx disposable error.{}###########", throwable.toString());
        }).doOnComplete(() -> {
            log.debug("##############vertx disposable success.###########");
        }).doFinally(() -> {
            log.debug("##############vertx disposable over.###########");
        }).subscribe();
        log.info("disposable bean shutdown hook.");
    }

    @Override
    public void onApplicationEvent(@NonNull ContextClosedEvent contextClosedEvent) {
        serviceDiscovery.close();
        vertx.close().doOnError(throwable -> {
            log.debug("##############vertx close error.{}###########", throwable.toString());
        }).doOnComplete(() -> {
            log.debug("##############vertx close success.###########");
        }).doFinally(() -> {
            log.debug("##############vertx close over.###########");
        }).subscribe();
        log.info("context closed event shutdown hook.");
    }
}
