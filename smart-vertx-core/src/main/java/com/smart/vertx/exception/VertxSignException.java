package com.smart.vertx.exception;

/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc vertx启动异常类
 */
public class VertxSignException extends RuntimeException {

    public VertxSignException(String message) {
        this(message, null);
    }

    public VertxSignException(String message, Throwable t) {
        super(message, t);
    }

}
