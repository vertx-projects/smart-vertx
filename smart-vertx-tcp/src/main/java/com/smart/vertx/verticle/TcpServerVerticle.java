package com.smart.vertx.verticle;

import com.smart.vertx.exception.VertxDeploymentException;
import com.smart.vertx.handler.TcpProperties;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Handler;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import static com.smart.vertx.VertxTcpAutoConfiguration.springContext;

/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc verticle 启动类
 */
@Slf4j
public class TcpServerVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart() {
        TcpProperties tcpProperties = springContext.getBean(TcpProperties.class);
        // 构造handler
        log.info("tcp server verticle is starting......");

        return Single.defer(() -> {
            Handler<NetSocket> handler = springContext.getBean(Handler.class);
            return Single.just(handler);
        }).flatMap(s -> {
            return vertx.createNetServer()
                    .connectHandler(s)
                    .rxListen(tcpProperties.getPort(), tcpProperties.getHost())
                    .doOnSuccess(p -> {
                        log.info("tcp server verticle  is started , port:{}", p.actualPort());
                    });
        }).onErrorComplete(throwable -> {
            log.error("failed to start tcp server verticle,{}", throwable.getLocalizedMessage(), throwable);
            throw new VertxDeploymentException("failed to start tcp server verticle", throwable);
        }).ignoreElement();
    }
}
