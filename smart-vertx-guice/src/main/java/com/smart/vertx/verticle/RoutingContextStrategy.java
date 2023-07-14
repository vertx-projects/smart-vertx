package com.smart.vertx.verticle;

import com.google.inject.Singleton;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author peng.bo
 * @date 2022/5/24 14:13
 * @desc
 */
@Singleton
public class RoutingContextStrategy implements IParamStrategy {
    @Override
    public Single<Object> init(List<Object> items, Parameter parameter, RoutingContext s) {
        if (parameter.getType().equals(s.getClass())) items.add(s);
        return Single.just(true);
    }

    @Override
    public String getParameter() {
        return RoutingContext.class.getName();
    }
}
