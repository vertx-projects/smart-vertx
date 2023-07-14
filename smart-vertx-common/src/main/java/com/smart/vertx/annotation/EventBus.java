package com.smart.vertx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author peng.bo
 * @date 2022/5/30 16:04
 * @desc
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventBus {
    String address() default "";

    String serviceName() default "";

    int timeOut() default 30000;
}
