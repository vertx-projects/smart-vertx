package com.smart.vertx.core;

import com.alibaba.fastjson2.JSON;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smart.vertx.enums.VerticleTypeEnum;
import com.smart.vertx.verticle.HttpServerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @author peng.bo
 * @date 2022/6/7 13:00
 * @desc
 */
@Slf4j
@Singleton
public class ClusterVerticle implements IVerticleType {
    @Inject
    VertxClusterProperties clusterProperties;
    @Inject
    VertxProperties vertxProperties;
    @Inject
    DeploymentOptions deploymentOptions;
    @Inject
    VertxOptions vertxOptions;

    @Override
    public void start(Vertx vertx) {
        log.info("vertx cluster Properties: {}", JSON.toJSONString(clusterProperties));
        // 创建vertx
        EventBusOptions eventBusOptions = new EventBusOptions();
        if (!clusterProperties.isSsl()) {
            eventBusOptions.setClusterPublicHost(vertxProperties.getHost());
        } else {
            eventBusOptions.setSsl(true).setKeyCertOptions(clusterProperties.getKeyStore()).setTrustOptions(clusterProperties.getTrustStore());
        }
        //集群方式启动时监听端口，用于接收数据
        vertxOptions.setEventBusOptions(eventBusOptions);
        //集群方式启动
        ClusterManager clusterManager = new IgniteClusterManager();
        vertxOptions.setClusterManager(clusterManager);
        log.info("vertx cluster is starting......");
        Vertx.rxClusteredVertx(vertxOptions).map(o -> {
            log.info("verticle is deploying......");
            o.rxDeployVerticle(HttpServerVerticle.class.getName(), deploymentOptions).doOnError(throwable -> {
                log.error(" verticle deploying error,{}", throwable.getLocalizedMessage());
            }).doOnSuccess(s -> {
                log.info("verticle [{}] is deployed.", s);
            }).subscribe(p -> {
                log.info("系统所有模块启动完成,服务状态健康,可对外服务,{}", p);
            }, throwable -> {
                log.error("启动服务失败", throwable);
                System.exit(1);
            });
            return o;
        }).doOnSuccess(s -> {
            log.info("vertx cluster is started.");
        }).doOnError(throwable -> {
            log.info("vertx cluster started error.{}", throwable.getLocalizedMessage(), throwable);
        }).subscribe();
    }

    @Override
    public VerticleTypeEnum name() {
        return VerticleTypeEnum.cluster;
    }
}
