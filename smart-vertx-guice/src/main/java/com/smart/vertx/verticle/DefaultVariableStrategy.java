package com.smart.vertx.verticle;

import com.google.inject.Singleton;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author peng.bo
 * @date 2022/5/24 13:43
 * @desc
 */
@Singleton
public class DefaultVariableStrategy implements IParamStrategy {
    @Override
    public Single<Object> init(List<Object> items, Parameter parameter, RoutingContext s) {
        String param = parameter.getName();
        Class<?> clazz = parameter.getType();
        Object value = getDefaultValue(clazz, s.request().getParam(param), s);
        items.add(value);
        return Single.just(true);
    }

    @Override
    public String getParameter() {
        return Object.class.getName();
    }

}
