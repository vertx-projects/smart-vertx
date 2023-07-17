package com.smart.vertx.annotation;


import com.smart.vertx.enums.RequestMethod;

import java.lang.annotation.*;

/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequestMapping {
    String value() default "";

    boolean blocked() default false;

    boolean blockOrdered() default false;

    RequestMethod method() default RequestMethod.GET;

    String description() default "no description";

    String returnDescription() default "no return description";

    String summary() default "no summary";

    int code() default -1;

    String contentType() default "application/json; charset=utf-8";

    String accept() default "*/*";
}
