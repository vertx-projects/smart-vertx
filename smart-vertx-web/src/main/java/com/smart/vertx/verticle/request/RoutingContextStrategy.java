package com.smart.vertx.verticle.request;

import io.vertx.rxjava3.ext.web.RoutingContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author peng.bo
 * @date 2022/5/24 14:13
 * @desc
 */
@Service
public class RoutingContextStrategy implements IParamStrategy {
    @Override
    public void init(List<Object> items, Parameter parameter, RoutingContext s) {
        if (parameter.getType().equals(s.getClass())) {
            items.add(s);
        }
    }

    @Override
    public String getParameter() {
        return RoutingContext.class.getName();
    }
}
