package com.smart.vertx.verticle;

import com.smart.vertx.exception.VertxDeploymentException;
import com.smart.vertx.handler.NetProxy;
import com.smart.vertx.handler.TcpProperties;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import static com.smart.vertx.VertxTcpAutoConfiguration.springContext;

/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc verticle 启动类
 */
@Slf4j
public class TcpClientVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart() {
        TcpProperties tcpProperties = springContext.getBean(TcpProperties.class);
        // 构造handler
        log.info("tcp client verticle is starting......");
        return vertx.createNetClient().rxConnect(tcpProperties.getPort(), tcpProperties.getHost()).doOnSuccess(p -> {
            ConfigurableApplicationContext context = (ConfigurableApplicationContext) springContext;
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(NetProxy.class);
            beanDefinitionBuilder.addPropertyValue("netSocket", p);
            //动态注入netSocket
            beanFactory.registerBeanDefinition(NetProxy.class.getName(), beanDefinitionBuilder.getBeanDefinition());
            log.info("tcp client verticle is started ,remote address:{}", p.remoteAddress());
        }).onErrorComplete(throwable -> {
            log.error("failed to start tcp client verticle,{}", throwable.getLocalizedMessage(), throwable);
            throw new VertxDeploymentException("failed to start tcp client verticle", throwable);
        }).ignoreElement();
    }
}
