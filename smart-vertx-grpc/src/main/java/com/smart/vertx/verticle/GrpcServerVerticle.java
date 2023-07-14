package com.smart.vertx.verticle;

import com.smart.vertx.exception.VertxDeploymentException;
import com.smart.vertx.GrpcProperties;
import io.vertx.core.AbstractVerticle;
import io.vertx.grpc.server.GrpcServer;
import lombok.extern.slf4j.Slf4j;

import static com.smart.vertx.VertxGrpcAutoConfiguration.springContext;

/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc verticle 启动类
 */
@Slf4j
public class GrpcServerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        GrpcProperties grpcProperties = springContext.getBean(GrpcProperties.class);
        GrpcServer grpcServer = springContext.getBean(GrpcProxy.class).getGrpcServer();
        // 构造handler
        log.info("grpc server verticle is starting......");
        vertx.createHttpServer()
                .requestHandler(grpcServer)
                .exceptionHandler(throwable -> {
                    log.error("grpc server invoke error,{}", throwable.getLocalizedMessage(), throwable);
                })
                .listen(grpcProperties.getPort(), grpcProperties.getHost())
                .onSuccess(p -> {
                    log.info("grpc server verticle is started , port:{}", p.actualPort());
                }).onFailure(throwable -> {
                    log.error("failed to start grpc server verticle,{}", throwable.getLocalizedMessage(), throwable);
                    throw new VertxDeploymentException("failed to start grpc server verticle", throwable);
                });
    }
}
