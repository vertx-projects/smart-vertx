package com.smart.vertx.ebean;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 支持序列化的Function
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.ebean
 * @date 2022/12/19 15:35
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}
