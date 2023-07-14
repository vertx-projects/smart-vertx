package com.smart.vertx.verticle.interceptor;

import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle.interceptor
 * @date 2023/4/7 16:37
 */
public interface HandlerInterceptor {
    default String pattern() {
        return "*";
    }

    boolean preHandler(RoutingContext request, Object handler);

    default void postHandler(RoutingContext request, Object handler) {

    }
}
