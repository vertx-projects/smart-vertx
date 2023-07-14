package com.smart.vertx.annotation;

import com.smart.vertx.CassandraHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.annotation
 * @date 2022/6/24 10:43
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({CassandraHandler.class})
public @interface EnableCassandra {
}
