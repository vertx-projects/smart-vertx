package com.smart.vertx.annotation;

import java.lang.annotation.*;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.annotation
 * @date 2022/7/5 17:27
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface BasicAuthentication {
    String id();

    String password();
}
