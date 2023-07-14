package com.smart.vertx;

import com.alibaba.fastjson2.JSON;
import com.smart.vertx.auth.AuthProperties;
import com.smart.vertx.client.WebClientProperties;
import com.smart.vertx.core.limit.LimitProperties;
import com.smart.vertx.messagecodes.ProtoMessageCodec;
import com.smart.vertx.core.OpentracingProperties;
import com.smart.vertx.core.VertxHandler;
import com.smart.vertx.servicediscovery.DiscoveryHandler;
import com.smart.vertx.core.sign.SignProperties;
import com.smart.vertx.verticle.HttpServerVerticle;
import com.smart.vertx.verticle.SuperVerticle;
import com.smart.vertx.verticle.VerticleHandler;
import com.smart.vertx.verticle.WorkVerticle;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.rxjava3.circuitbreaker.CircuitBreaker;
import io.vertx.rxjava3.circuitbreaker.RetryPolicy;
import io.vertx.rxjava3.core.Context;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.eventbus.EventBus;
import io.vertx.rxjava3.core.file.FileSystem;
import io.vertx.rxjava3.core.shareddata.SharedData;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.rxjava3.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Map;


/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc Vertx Http Server自动配置类
 */
@Slf4j
@AutoConfigureAfter(VertxHandler.class)
@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties({VertxProperties.class, OpentracingProperties.class, AuthProperties.class, WebClientProperties.class, SignProperties.class, LimitProperties.class})
@Import({VerticleHandler.class, DiscoveryHandler.class, VertxHandler.class})
public class VertxAutoConfiguration {

    public VertxAutoConfiguration(ApplicationContext springContext) {
        SuperVerticle.springContext = springContext;
    }

    @Bean
    @ConditionalOnBean({VertxOptions.class, DeploymentOptions.class, VertxClusterProperties.Maker.class})
    public Vertx cluster(VertxProperties vertxProperties, VertxOptions vertxOptions, DeploymentOptions deploymentOptions, VertxClusterProperties clusterProperties, ClusterManager clusterManager) {
        log.info("vertx vertxProperties: {}", JSON.toJSONString(vertxProperties));
        log.info("vertx cluster Properties: {}", JSON.toJSONString(clusterProperties));
        // 创建vertx
        EventBusOptions eventBusOptions = new EventBusOptions();
        if (!clusterProperties.isSsl()) {
            eventBusOptions.setClusterPublicHost(vertxProperties.getHost());
        } else {
            eventBusOptions.setSsl(true).setKeyCertOptions(clusterProperties.getKeyStore()).setTrustOptions(clusterProperties.getTrustStore());
        }
        vertxOptions.setClusterManager(clusterManager);
        log.info("vertx cluster is starting......");
        return Vertx.rxClusteredVertx(vertxOptions).map(o -> deploy(deploymentOptions, o, HttpServerVerticle.class))
                .doOnSuccess(s -> log.info("vertx cluster is started."))
                .doOnError(throwable -> log.info("vertx cluster started error.{}", throwable.getLocalizedMessage(), throwable))
                .blockingGet();
    }

    @Bean
    public EventBus eventBus(Vertx vertx) {
        ProtoMessageCodec protoMessageCodec = new ProtoMessageCodec();
        return vertx.eventBus().registerCodec(protoMessageCodec);
    }

    @Bean
    @ConditionalOnBean({VertxOptions.class, DeploymentOptions.class})
    @ConditionalOnMissingBean(name = "cluster")
    public Vertx standalone(VertxProperties config, VertxOptions vertxOptions, DeploymentOptions deploymentOptions) {
        log.info("vertx config: {}", JSON.toJSONString(config));
        Vertx vertx = Vertx.vertx(vertxOptions);
        return deploy(deploymentOptions, vertx, HttpServerVerticle.class);
    }

    @Bean
    public ServiceDiscovery discovery(Vertx vertx) {
        return ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setUsageAddress(null));
    }

    @Bean
    public WebClient webClient(Vertx vertx) {
        return WebClient.create(vertx);
    }

    @Bean
    public SharedData sharedData(Vertx vertx) {
        return vertx.sharedData();
    }

    @Bean
    public FileSystem fileSystem(Vertx vertx) {
        return vertx.fileSystem();
    }

    @Bean
    public Context context(Vertx vertx) {
        return vertx.getOrCreateContext();
    }

    @Bean
    public CircuitBreaker circuitBreaker(Vertx vertx) {
        return CircuitBreaker.create("smart-circuit-breaker", vertx, new CircuitBreakerOptions()
                        .setNotificationAddress(CircuitBreakerOptions.DEFAULT_NOTIFICATION_ADDRESS)
                        .setMaxRetries(2) //最大重试次数
                        .setMaxFailures(2) // 最大失败数
                        .setTimeout(2000) // 超时时间
                        .setFallbackOnFailure(true) // 失败后是否调用回退函数（fallback）
                        .setResetTimeout(10000) // 在开启状态下，尝试重试之前所需时间
                )
                .openHandler(v -> log.warn("vertx circuit opened."))
                .closeHandler(v -> log.info("vertx circuit closed."))
                .halfOpenHandler(v -> log.warn("vertx circuit half open."))
                //抖动的延时补偿，重试超时时间与重试时间呈线指数增长，每重试一次时间+50毫秒
                .retryPolicy(RetryPolicy.exponentialDelayWithJitter(50L, 500L));
    }

    private Vertx deploy(DeploymentOptions deploymentOptions, Vertx vertx, Class<?> clazz) {
        log.info("verticle [{}] is deploying......", clazz);
        vertx.rxDeployVerticle(clazz.getName(), deploymentOptions).map(s -> {
                    deployWorkVerticle(deploymentOptions, vertx);
                    return s;
                }).onErrorComplete(throwable -> {
                    log.error(" verticle deploying error,{}", throwable.getLocalizedMessage());
                    return true;
                }).doOnSuccess(s -> log.info("verticle [{}.{}] is deployed.", clazz, s))
                .subscribe(o -> log.info("系统所有模块启动完成:服务状态健康,可对外服务,PID={}.", o), throwable -> {
                    log.error("启动服务失败", throwable);
                    System.exit(1);
                });
        return vertx;
    }

    private void deployWorkVerticle(DeploymentOptions deploymentOptions, Vertx vertx) {
        try {
            Map<String, WorkVerticle> workVerticle = SuperVerticle.springContext.getBeansOfType(WorkVerticle.class);
            workVerticle.values().forEach(v -> {
                deploymentOptions.setWorker(v.worker()).setHa(v.ha());
                deploy(deploymentOptions, vertx, v.getClass());
                log.info("work verticle {} started.", v);
            });
        } catch (Exception ignored) {
            log.info("ignore  all work verticle.");
        }
    }
}
