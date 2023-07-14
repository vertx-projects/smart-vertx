package com.smart.vertx.annotation;

import java.lang.annotation.*;

/**
 * @author peng.bo
 * @date 2022/5/23 19:12
 * @desc
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathVariable {
    String value() default "";

    String name() default "";

    boolean required() default true;
}
