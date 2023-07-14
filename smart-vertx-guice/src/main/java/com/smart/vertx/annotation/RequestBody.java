package com.smart.vertx.annotation;

import java.lang.annotation.*;

/**
 * @author peng.bo
 * @date 2022/5/23 17:52
 * @desc
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBody {
    boolean valid() default true;
}
