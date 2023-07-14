package com.smart.vertx.annotation;

import com.smart.vertx.KafkaHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开启vertx集群模式
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({KafkaHandler.class})
public @interface EnableKafka {
    boolean producer() default false;

    boolean consumer() default true;
}