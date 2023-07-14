package com.smart.vertx.annotation;

import java.lang.annotation.*;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.annotation
 * @date 2022/6/24 16:12
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginUser {
}
