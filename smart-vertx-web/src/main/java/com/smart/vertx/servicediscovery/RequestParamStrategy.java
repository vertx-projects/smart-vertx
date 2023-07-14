package com.smart.vertx.servicediscovery;

import com.smart.vertx.annotation.RequestParam;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * @author peng.bo
 * @date 2022/5/24 13:43
 * @desc
 */
@Service("requestParam")
public class RequestParamStrategy implements IParamStrategy {
    @Override
    public Object init(Object data, Parameter parameter, Single<HttpRequest<Buffer>> request) {
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (data instanceof Map) {
            return request.map(s -> {
                Map<String, Object> map = (Map) data;
                map.forEach((k, v) -> {
                    s.addQueryParam(k, v.toString());
                });
                return s;
            });
        } else {
            String param = StringUtils.isBlank(requestParam.value()) ? parameter.getName() : requestParam.value();
            return request.map(s -> s.addQueryParam(param, data.toString()));
        }
    }

    @Override
    public String getParameter() {
        return RequestParam.class.getName();
    }
}
