package com.smart.vertx.verticle.request;

import com.alibaba.fastjson2.JSON;
import com.smart.vertx.annotation.RequestBody;
import com.smart.vertx.exception.BusinessException;
import com.smart.vertx.exception.CoreExceptionCodes;
import com.smart.vertx.util.BeanValidatorsUtil;
import com.smart.vertx.util.JacksonUtil;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import static com.smart.vertx.util.JacksonUtil.JSON_SERIALIZE;

/**
 * @author peng.bo
 * @date 2022/5/24 11:54
 * @desc
 */
@Service
public class RequestBodyStrategy implements IParamStrategy {
    @Override
    public void init(List<Object> items, Parameter parameter, RoutingContext s) {
        RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
        if (requestBody != null) {
            if (s.body().isEmpty()) {
                throw new BusinessException(CoreExceptionCodes.PARAM_IS_NULL);
            }
            Object to = StringUtils.isBlank(System.getProperty(JSON_SERIALIZE)) ?
                    JSON.to(parameter.getType(), s.body().asString()) :
                    JacksonUtil.fromString(s.body().asString(), parameter.getType());
            if (requestBody.valid()) {
                Map<String, String> errors = BeanValidatorsUtil.extractPropertyAndMessage(VALIDATOR.validate(to, new Class[0]));
                if (!errors.isEmpty()) {
                    throw new BusinessException(CoreExceptionCodes.PARAM_IS_ILLEGAL, errors);
                }
            }
            items.add(to);
        }
    }

    @Override
    public String getParameter() {
        return RequestBody.class.getName();
    }
}
