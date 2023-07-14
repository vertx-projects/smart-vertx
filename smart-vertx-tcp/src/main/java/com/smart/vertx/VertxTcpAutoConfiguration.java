package com.smart.vertx;

import com.alibaba.fastjson2.JSON;
import com.smart.vertx.core.VertxHandler;
import com.smart.vertx.handler.TcpProperties;
import com.smart.vertx.verticle.TcpClientVerticle;
import com.smart.vertx.verticle.TcpServerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.VertxOptions;
import io.vertx.rxjava3.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;


/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc Vertx Http Server自动配置类
 */
@Slf4j
@Configuration
@AutoConfigureAfter(VertxHandler.class)
@EnableConfigurationProperties({VertxProperties.class, TcpProperties.class})
@Import({VertxHandler.class})
public class VertxTcpAutoConfiguration {
    public static ApplicationContext springContext;

    public VertxTcpAutoConfiguration(ApplicationContext springContext) {
        VertxTcpAutoConfiguration.springContext = springContext;
    }

    @Bean
    @ConditionalOnBean({VertxOptions.class, TcpProperties.Server.class, Handler.class})
    @Primary
    public Vertx tcpServer(VertxProperties config, TcpProperties tcpProperties, VertxOptions vertxOptions, DeploymentOptions deploymentOptions) {
        log.info("vertx config: {}", JSON.toJSONString(config));
        log.info("tcp config: {}", JSON.toJSONString(tcpProperties));
        Vertx vertx = Vertx.vertx(vertxOptions);
        log.info("verticle is deploying......");
        vertx.rxDeployVerticle(TcpServerVerticle.class.getName(), deploymentOptions).doOnError(throwable -> {
            log.error(" verticle deploying error,{}", throwable.getLocalizedMessage());
        }).doOnSuccess(s -> {
            log.info("verticle [{}] is deployed.", s);
        }).subscribe(o -> {
            log.info("系统所有模块启动完成,服务状态健康,可对外服务,{}", o);
        }, throwable -> {
            log.error("启动服务失败", throwable);
            System.exit(1);
        });
        return vertx;
    }

    @Bean
    @ConditionalOnBean({VertxOptions.class, TcpProperties.Client.class})
    @Primary
    public Vertx tcpClient(VertxProperties config, TcpProperties tcpProperties, VertxOptions vertxOptions, DeploymentOptions deploymentOptions) {
        log.info("vertx config: {}", JSON.toJSONString(config));
        log.info("tcp config: {}", JSON.toJSONString(tcpProperties));
        Vertx vertx = Vertx.vertx(vertxOptions);
        log.info("verticle is deploying......");
        vertx.rxDeployVerticle(TcpClientVerticle.class.getName(), deploymentOptions).doOnError(throwable -> {
            log.error(" verticle deploying error,{}", throwable.getLocalizedMessage());
        }).doOnSuccess(s -> {
            log.info("verticle [{}] is deployed.", s);
        }).subscribe(o -> {
            log.info("系统所有模块启动完成,服务状态健康,可对外服务,{}", o);
        }, throwable -> {
            log.error("启动服务失败", throwable);
            System.exit(1);
        });
        return vertx;
    }
}
