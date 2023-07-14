package com.smart.vertx.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;

import java.lang.annotation.*;

/**
 * @author peng.bo
 * @date 2022/5/18 10:02
 * @desc
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
public @interface RestController {
    @AliasFor("path")
    String value() default "";
    @AliasFor("value")
    String path() default "";
}
