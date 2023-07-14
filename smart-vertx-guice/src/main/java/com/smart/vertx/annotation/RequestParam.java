package com.smart.vertx.annotation;

import java.lang.annotation.*;

/**
 * @author peng.bo
 * @date 2022/5/25 11:41
 * @desc
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    String value() default "";

    String name() default "";

    boolean required() default true;

    String defaultValue() default "\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n";
}
