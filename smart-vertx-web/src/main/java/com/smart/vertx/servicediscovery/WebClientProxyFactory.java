package com.smart.vertx.servicediscovery;

import com.google.common.collect.Maps;
import com.smart.vertx.annotation.WebClient;
import com.smart.vertx.annotation.RequestMapping;
import com.smart.vertx.servicediscovery.strategy.RequestStrategyAbstract;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.*;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author peng.bo
 * @date 2022/5/30 17:07
 * @desc
 */
@Slf4j
public class WebClientProxyFactory<T> implements FactoryBean<T>, MethodInterceptor {
    @Setter
    @Getter
    private Class<T> superclass;
    private final Enhancer enhancer = new Enhancer();
    public static final Map<String, Object> fallbackMap = Maps.newConcurrentMap();

    @Override
    public T getObject() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        WebClient webClient = superclass.getAnnotation(WebClient.class);
        if (!void.class.equals(webClient.fallback())) {
            fallbackMap.putIfAbsent(superclass.getName(), webClient.fallback().getDeclaredConstructor().newInstance());
        }
        enhancer.setClassLoader(this.getClass().getClassLoader());
        enhancer.setSuperclass(getSuperclass());
        enhancer.setCallback(this);
        return (T) enhancer.create();
    }

    @Override
    public Class<?> getObjectType() {
        return superclass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * cglib代理
     */
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) {
        Class<?> targetCls = method.getDeclaringClass();
        WebClient webClient = targetCls.getAnnotation(WebClient.class);
        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        return RequestStrategyAbstract.get(isBlank(webClient.url())).invoke(method, args, webClient, mapping);
    }
}
