package com.smart.vertx.annotation;

import com.smart.vertx.handler.TcpCallerMarkerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启TCP服务
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({TcpCallerMarkerRegistrar.class})
public @interface EnableTcp {
    boolean server() default true;
}