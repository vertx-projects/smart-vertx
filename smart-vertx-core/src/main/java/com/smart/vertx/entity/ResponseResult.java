package com.smart.vertx.entity;


import com.alibaba.fastjson2.JSON;
import com.smart.vertx.exception.CoreExceptionCodes;
import com.smart.vertx.util.JacksonUtil;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static com.smart.vertx.util.JacksonUtil.JSON_SERIALIZE;


/**
 * @brief： 响应结果集模板类
 * @author: pengbo
 * @since: 2019-07-13
 */
@Data
public class ResponseResult<T> implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return StringUtils.isBlank(System.getProperty(JSON_SERIALIZE)) ?
                JSON.toJSONString(this) : JacksonUtil.toString(this);
    }

    /**
     * 返回的数据
     */
    private T data;
    private Integer code;
    private String message;

    private Object stack;

    public ResponseResult(T data) {
        this(CoreExceptionCodes.OK.code(), CoreExceptionCodes.OK.reasonPhrase(), data);
    }

    public ResponseResult(Integer code, String message) {
        this(code, message, null);
    }

    public ResponseResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 包装成功返回的对象
     *
     * @param data 数据对象
     * @return ResponseResult<T> 返回的结果对象
     */
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(data);
    }

    /**
     * 包装错误返回结果
     *
     * @param code    错误编码
     * @param message 错误信息
     * @return ResponseResult<T> 返回的结果对象
     */
    public static <T> ResponseResult<T> fail(Integer code, String message) {
        return new ResponseResult<>(code, message);
    }

    public static <T> ResponseResult<T> fail(HttpResponseStatus status, T data) {
        return new ResponseResult<>(status.code(), status.reasonPhrase(), data);
    }

    public static <T> ResponseResult<T> fail(Integer code, String message, T data) {
        return new ResponseResult<>(code, message, data);
    }

    /**
     * 判断返回的结果编码是否正确
     *
     * @return boolean 结果是否成功
     */
    public boolean checkSuccess() {
        return code.equals(CoreExceptionCodes.OK.code());
    }

    public Object getData() {
        if (data instanceof JsonObject) {
            return ((JsonObject) data).getMap();
        } else if (data instanceof List) {
            List<?> items = (List<?>) data;
            return items.stream().map(s -> {
                if (s instanceof JsonObject) {
                    return ((JsonObject) s).getMap();
                }
                return s;
            }).collect(Collectors.toList());
        }
        return data;
    }
}
