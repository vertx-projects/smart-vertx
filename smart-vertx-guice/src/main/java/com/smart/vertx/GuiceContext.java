package com.smart.vertx;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.smart.vertx.core.*;
import com.smart.vertx.enums.VerticleTypeEnum;
import com.smart.vertx.utils.CommonUtil;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.eventbus.EventBus;
import io.vertx.rxjava3.servicediscovery.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.smart.vertx.constant.CommonConstant.*;

/**
 * @author peng.bo
 * @date 2022/6/7 13:46
 * @desc
 */
@Slf4j
public class GuiceContext {
    public static Injector injector;

    public static void run(VerticleTypeEnum typeEnum, Module... modules) {
        List<Module> items = Lists.newArrayList();
        final VertxProperties properties = getVertxProperties(items);
        log.info("vertx properties: {}", JSON.toJSONString(properties));
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setEventLoopPoolSize(properties.getNioThreadCount()).setWorkerPoolSize(properties.getWorkerThreadCount()).setMaxEventLoopExecuteTime(properties.getMaxEventLoopExecuteTime()).setMaxEventLoopExecuteTimeUnit(TimeUnit.MILLISECONDS);
        Vertx vertx = Vertx.vertx(vertxOptions);
        final ServiceDiscovery serviceDiscovery = ServiceDiscovery.create(vertx);
        log.info("verticle is deploying......");
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setInstances(properties.getVerticleCount());
        items.add(new AbstractModule() {
            @Override
            protected void configure() {
                if (typeEnum.equals(VerticleTypeEnum.cluster)) {
                    bind(VertxClusterProperties.class).toInstance(getClusterProperties());
                }
                bind(VertxProperties.class).toInstance(properties);
                bind(DeploymentOptions.class).toInstance(deploymentOptions);
                bind(VertxOptions.class).toInstance(vertxOptions);
                bind(ServiceDiscovery.class).toInstance(serviceDiscovery);
                bind(Vertx.class).toInstance(vertx);
                bind(EventBus.class).toInstance(vertx.eventBus());
            }
        });
        Optional.ofNullable(modules).ifPresent(s -> {
            items.addAll(Arrays.asList(s));
        });
        injector = Guice.createInjector(items.toArray(new Module[]{}));
        GuiceFactory guiceFactory = injector.getInstance(GuiceFactory.class);
        guiceFactory.runApplication(vertx, typeEnum);
    }

    public static <T> T getBean(Class<T> t) {
        return injector.getInstance(t);
    }

    private static VertxProperties getVertxProperties(List<Module> items) {
        VertxProperties properties = JSON.to(VertxProperties.class, loadYaml());
        properties = Objects.isNull(properties) ? new VertxProperties() : properties;
        Set<Class<?>> sets = CommonUtil.getClasses(properties.getPackageName());
        for (Class<?> s : sets) {
            if (s.getGenericSuperclass() instanceof AbstractModule) {
                try {
                    items.add((AbstractModule) s.getDeclaredConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    log.error("get AbstractModule instance error,", e);
                }
            }
        }
        items.add(new ParamStrategyModule());
        items.add(new VerticleModule());
        return properties;
    }

    private static Object loadYaml() {
        Yaml yaml = new Yaml();
        InputStream inputStream = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(BOOTSTRAP));
        try {
            inputStream = new FileInputStream(CONFIG_BOOTSTRAP);
        } catch (FileNotFoundException e) {
            log.warn("######load {} file is empty ignore this config######,", CONFIG_BOOTSTRAP);
        }
        Map<String, Object> maps = yaml.load(inputStream);
        if (Objects.isNull(maps)) {
            return Maps.newHashMap();
        }
        return maps.get(VERTX);
    }

    private static VertxClusterProperties getClusterProperties() {
        Object data = loadYaml();
        if (Objects.nonNull(data)) {
            data = ((Map<?, ?>) data).get(CLUSTER);
        }
        VertxClusterProperties properties = JSON.to(VertxClusterProperties.class, data);
        return Objects.isNull(properties) ? new VertxClusterProperties() : properties;
    }
}
