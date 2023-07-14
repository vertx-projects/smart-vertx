package com.smart.vertx;

import com.smart.vertx.annotation.EnableGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author peng.bo
 * @date 2022/5/19 13:28
 * @desc
 */
@Slf4j
public class GrpcCallerMarkerRegistrar extends GrpcCallerRegistrar<EnableGrpc> {

    @Override
    protected void selectImports(boolean server, BeanDefinitionRegistry registry) {
        if (server) {
            registry.registerBeanDefinition(GrpcProperties.Server.class.getName(), new RootBeanDefinition(GrpcProperties.Server.class));
        } else {
            registry.registerBeanDefinition(GrpcProperties.Client.class.getName(), new RootBeanDefinition(GrpcProperties.Client.class));
        }
    }
}
