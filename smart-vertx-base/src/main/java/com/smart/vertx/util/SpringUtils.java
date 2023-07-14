package com.smart.vertx.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.Collection;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.util
 * @date 2022/8/1 15:13
 */
public class SpringUtils {
    public static <T> T getBean(ApplicationContext springContext, Class<T> aClass) {
        try {
            return springContext.getBean(aClass);
        } catch (BeansException e) {
            return null;
        }
    }
    public static <T> Collection<T> getBeans(ApplicationContext springContext, Class<T> aClass) {
        try {
            return springContext.getBeansOfType(aClass).values();
        } catch (BeansException e) {
            return null;
        }
    }
}
