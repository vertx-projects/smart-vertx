package com.smart.vertx.exception;

/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc vertx启动异常类
 */
public class VertxDeploymentException extends RuntimeException {

    public VertxDeploymentException(String message) {
        this(message, null);
    }

    public VertxDeploymentException(String message, Throwable t) {
        super(message, t);
    }

}
