package com.smart.vertx.annotation;


import com.google.inject.Singleton;

import java.lang.annotation.*;

/**
 * @author peng.bo
 * @date 2022/5/18 10:02
 * @desc
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Singleton
public @interface RestController {
    String value() default "";

    String path() default "";
}
