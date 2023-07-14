package com.smart.vertx.annotation;

import com.smart.vertx.core.limit.LimitHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.core.limit
 * @date 2022/7/28 18:25
 */
@Retention(RetentionPolicy.RUNTIME)
@Import(LimitHandler.class)
@Documented
public @interface EnableLimit {
}
