package com.smart.vertx.verticle.request;

import io.vertx.rxjava3.ext.web.RoutingContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author peng.bo
 * @date 2022/5/24 13:43
 * @desc
 */
@Service
public class DefaultVariableStrategy implements IParamStrategy {
    @Override
    public void init(List<Object> items, Parameter parameter, RoutingContext s) {
        String param = parameter.getName();
        Class<?> clazz = parameter.getType();
        Object value = getDefaultValue(clazz, s.request().getParam(param), s);
        items.add(value);
    }

    @Override
    public String getParameter() {
        return Object.class.getName();
    }

}
