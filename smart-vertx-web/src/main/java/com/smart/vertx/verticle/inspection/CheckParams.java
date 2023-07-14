package com.smart.vertx.verticle.inspection;

import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle.inspection
 * @date 2022/8/1 16:04
 */
@Data
@Builder
public class CheckParams {
    private RoutingContext s;
    private Method method;
}
