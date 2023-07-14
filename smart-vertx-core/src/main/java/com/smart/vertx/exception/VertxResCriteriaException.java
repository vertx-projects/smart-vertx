package com.smart.vertx.exception;

/**
 * @brief:   Web请求响应异常类
 * @author:  pengbo
 * @date:    2019-07-13
 */
public class VertxResCriteriaException extends RuntimeException {
    
    public VertxResCriteriaException(String message) {
        super(message);
    }
    public VertxResCriteriaException(String message, Throwable t) {
        super(message, t);
    }

}
