package com.smart.vertx.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

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
@Service
public @interface WebClient {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    String path() default "";

    String url() default "";

    Class<?> fallback() default void.class;
}
