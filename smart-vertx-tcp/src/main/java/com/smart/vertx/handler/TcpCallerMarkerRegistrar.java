package com.smart.vertx.handler;

import com.smart.vertx.annotation.EnableTcp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author peng.bo
 * @date 2022/5/19 13:28
 * @desc
 */
@Slf4j
public class TcpCallerMarkerRegistrar extends TcpCallerRegistrar<EnableTcp> {

    @Override
    protected void selectImports(boolean server, BeanDefinitionRegistry registry) {
        if (server) {
            registry.registerBeanDefinition(TcpProperties.Server.class.getName(), new RootBeanDefinition(TcpProperties.Server.class));
        } else {
            registry.registerBeanDefinition(TcpProperties.Client.class.getName(), new RootBeanDefinition(TcpProperties.Client.class));
        }
    }
}
