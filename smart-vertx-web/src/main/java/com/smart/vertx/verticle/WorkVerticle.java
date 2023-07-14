package com.smart.vertx.verticle;

import io.vertx.rxjava3.core.AbstractVerticle;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle
 * @date 2022/7/8 12:12
 */
public abstract class WorkVerticle extends AbstractVerticle {
    public boolean worker() {
        return false;
    }
    public boolean ha() {
        return false;
    }
}
