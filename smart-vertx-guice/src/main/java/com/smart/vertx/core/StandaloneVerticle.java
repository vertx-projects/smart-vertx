package com.smart.vertx.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smart.vertx.enums.VerticleTypeEnum;
import com.smart.vertx.verticle.HttpServerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.rxjava3.core.Vertx;
import lombok.extern.slf4j.Slf4j;

/**
 * @author peng.bo
 * @date 2022/6/7 13:00
 * @desc
 */
@Slf4j
@Singleton
public class StandaloneVerticle implements IVerticleType {
    @Inject
    DeploymentOptions deploymentOptions;

    @Override
    public void start(Vertx vertx) {
        vertx.rxDeployVerticle(HttpServerVerticle.class.getName(), deploymentOptions).doOnError(throwable -> {
            log.error(" verticle deploying error,{}", throwable.getLocalizedMessage());
        }).doOnSuccess(s -> {
            log.info("verticle [{}] is deployed.", s);
        }).subscribe(o -> {
            log.info("系统所有模块启动完成,服务状态健康,可对外服务,{}", o);
        }, throwable -> {
            log.error("启动服务失败", throwable);
            System.exit(1);
        });
    }

    @Override
    public VerticleTypeEnum name() {
        return VerticleTypeEnum.standalone;
    }
}
