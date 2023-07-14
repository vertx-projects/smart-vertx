package com.smart.vertx.verticle;

import com.alibaba.fastjson2.JSON;
import com.google.inject.Singleton;
import com.smart.vertx.annotation.RequestBody;
import com.smart.vertx.entity.ResponseResult;
import com.smart.vertx.exception.CoreExceptionCodes;
import com.smart.vertx.util.BeanValidatorsUtil;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

/**
 * @author peng.bo
 * @date 2022/5/24 11:54
 * @desc
 */
@Singleton
public class RequestBodyStrategy implements IParamStrategy {
    @Override
    public Single<Object> init(List<Object> items, Parameter parameter, RoutingContext s) {
        RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
        if (requestBody != null) {
            Object to = JSON.to(parameter.getType(), s.body().asString());
            if (requestBody.valid()) {
                Map<String, String> errors = BeanValidatorsUtil.extractPropertyAndMessage(VALIDATOR.validate(to, new Class[0]));
                if (!errors.isEmpty()) {
                    return Single.just(ResponseResult.fail(CoreExceptionCodes.PARAM_IS_ILLEGAL, errors));
                }
            }
            items.add(to);
        }
        return Single.just(true);
    }

    @Override
    public String getParameter() {
        return RequestBody.class.getName();
    }


}
