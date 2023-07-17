package com.smart.vertx;

import com.alibaba.fastjson2.JSON;
import com.smart.vertx.core.VertxHandler;
import com.smart.vertx.verticle.GrpcProxy;
import com.smart.vertx.verticle.GrpcServerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.rxjava3.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
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
@EnableConfigurationProperties({VertxProperties.class, GrpcProperties.class})
@Import(VertxHandler.class)
public class VertxGrpcAutoConfiguration {
    public static ApplicationContext springContext;

    public VertxGrpcAutoConfiguration(ApplicationContext springContext) {
        VertxGrpcAutoConfiguration.springContext = springContext;
    }

    @Bean
    @ConditionalOnBean({VertxOptions.class, GrpcProperties.Server.class})
    @Primary
    public Vertx grpcServer(VertxProperties config, GrpcProperties grpcProperties, VertxOptions vertxOptions, DeploymentOptions deploymentOptions) {
        log.info("vertx config: {}", JSON.toJSONString(config));
        log.info("grpc config: {}", JSON.toJSONString(grpcProperties));
        Vertx vertx = Vertx.vertx(vertxOptions);
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) springContext;
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(GrpcProxy.class);
        beanDefinitionBuilder.addPropertyValue("grpcServer", GrpcServer.server(vertx.getDelegate()));
        //动态注入netSocket
        beanFactory.registerBeanDefinition(GrpcProxy.class.getName(), beanDefinitionBuilder.getBeanDefinition());
        log.info("verticle is deploying......");
        vertx.rxDeployVerticle(GrpcServerVerticle.class.getName(), deploymentOptions).doOnError(throwable -> {
            log.error(" verticle deploying error,{}", throwable.getLocalizedMessage());
        }).doOnSuccess(s -> {
            log.info("verticle [{}] is deployed.", s);
        }).subscribe(o -> {
            log.info("[GRPC]系统所有模块启动完成,服务状态健康,可对外服务,{}", o);
        }, throwable -> {
            log.error("启动服务失败", throwable);
            System.exit(1);
        });
        return vertx;
    }


    @Bean
    @ConditionalOnBean({Vertx.class, GrpcProperties.Client.class})
    public GrpcProxy grpcProxy(Vertx vertx, GrpcProperties grpcProperties) {
        GrpcClient grpcClient = GrpcClient.client(vertx.getDelegate());
        SocketAddress socketAddress = SocketAddress.inetSocketAddress(grpcProperties.getPort(), grpcProperties.getHost());
        return new GrpcProxy(socketAddress, grpcClient);
    }
}
