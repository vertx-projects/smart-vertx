package com.smart.vertx.exception;


import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.Serializable;

/**
 * @brief: 通用错误码定义类
 * @Description: 工具类型异常编码;编码规则: 业务系统前缀（3~5位大写字母，与项目英文简称相同）
 * <li>+2位错误类别（认证权限类10、参数错误类20，错误类别00~59为系统保留，60~99由业务自行定义）</li>
 * <li>+3位错误编码</li>
 * @author: pengbo
 * @since: 2019-07-15
 */
public class CoreExceptionCodes extends HttpResponseStatus implements Serializable {
    /**
     * 参数为空 10001
     */
    public static final HttpResponseStatus PARAM_IS_NULL = new HttpResponseStatus(10001, "参数为空");
    /**
     * 参数非法 10002
     */
    public static final HttpResponseStatus PARAM_IS_ILLEGAL = new HttpResponseStatus(10002, "参数非法");
    /**
     * token为空 10003
     */
    public static final HttpResponseStatus TOKEN_ERROR = new HttpResponseStatus(10003, "token不能为空");
    /**
     * 认证失败 10004
     */
    public static final HttpResponseStatus AUTH_ERROR = new HttpResponseStatus(10004, "认证失败");

    public CoreExceptionCodes(int code, String reasonPhrase) {
        super(code, reasonPhrase);
    }
}
