package com.smart.vertx.exception;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * @brief: 业务异常定义类
 * @author: pengbo
 * @date: 2019-07-13
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final Integer code;

    public Integer code() {
        return this.code;
    }

    public BusinessException(Integer code, String message) {
        this(code, message, null);
    }

    public BusinessException(Integer code, Throwable t) {
        this(code, null, t);
    }

    public BusinessException(Integer code, String message, Throwable t) {
        super(message, t);
        this.code = code;
    }

    public BusinessException(Throwable t) {
        this(CoreExceptionCodes.INTERNAL_SERVER_ERROR.code(), t);
    }

    public BusinessException(HttpResponseStatus status) {
        this(status.code(), status.reasonPhrase(), null);
    }

    public BusinessException(HttpResponseStatus status, Throwable t) {
        this(status.code(), status.reasonPhrase(), t);
    }

    public BusinessException(HttpResponseStatus status, Map<String, String> paramValues) {
        this(status.code(), getMessage(status.reasonPhrase(), paramValues), null);
    }

    private static String getMessage(String msg, Map<String, String> paramValues) {
        Map.Entry<String, String> entry;
        for (Iterator<Map.Entry<String, String>> var4 = paramValues.entrySet().iterator(); var4.hasNext(); msg = StringUtils.replacePattern(msg, "\\$\\{" + entry.getKey() + "\\}", entry.getValue())) {
            entry = var4.next();
        }
        return msg;
    }
}
