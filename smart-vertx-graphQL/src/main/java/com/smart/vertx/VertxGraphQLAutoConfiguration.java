package com.smart.vertx;

import com.smart.vertx.core.VertxHandler;
import com.smart.vertx.core.sign.SignProperties;
import com.smart.vertx.exception.VertxDeploymentException;
import com.smart.vertx.util.SpringUtils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.Json;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.Objects;


/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc Vertx Http Server自动配置类
 */
@Slf4j
@Configuration
@AutoConfigureAfter(VertxHandler.class)
@EnableConfigurationProperties({VertxProperties.class, SignProperties.class})
@Import(VertxHandler.class)
public class VertxGraphQLAutoConfiguration {
    public static ApplicationContext springContext;

    public VertxGraphQLAutoConfiguration(ApplicationContext springContext) {
        VertxGraphQLAutoConfiguration.springContext = springContext;
    }

    @Bean
    @ConditionalOnBean({VertxOptions.class})
    @Primary
    public Vertx graphQLServer(VertxProperties config, VertxOptions vertxOptions,
                            DeploymentOptions deploymentOptions) {
        log.info("vertx config: {}", Json.encode(config));
        Vertx vertx = Vertx.vertx(vertxOptions);
        log.info("verticle is deploying......");
        vertx.rxDeployVerticle(new GraphQLVerticle(springContext), deploymentOptions).doOnError(throwable -> {
            log.error(" verticle deploying error,{}", throwable.getLocalizedMessage());
        }).doOnSuccess(s -> {
            log.info("verticle [{}] is deployed.", s);
        }).subscribe(o -> {
            log.info("[GRAPHS_QL]系统所有模块启动完成,服务状态健康,可对外服务,{}", o);
        }, throwable -> {
            log.error("启动服务失败", throwable);
            System.exit(1);
        });
        return vertx;
    }

    @Bean
    public ServiceDiscovery discovery(Vertx vertx) {
        return ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setUsageAddress(null));
    }
}
