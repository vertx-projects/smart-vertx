package com.smart.vertx.core;

import com.smart.vertx.enums.VerticleTypeEnum;
import io.vertx.rxjava3.core.Vertx;

/**
 * @author peng.bo
 * @date 2022/6/7 12:58
 * @desc
 */
public interface IVerticleType {
    void start(Vertx vertx);

    VerticleTypeEnum name();
}
