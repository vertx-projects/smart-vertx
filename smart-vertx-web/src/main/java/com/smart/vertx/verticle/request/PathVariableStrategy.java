package com.smart.vertx.verticle.request;

import com.smart.vertx.annotation.PathVariable;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author peng.bo
 * @date 2022/5/24 13:43
 * @desc
 */
@Service
public class PathVariableStrategy implements IParamStrategy {
    @Override
    public void init(List<Object> items, Parameter parameter, RoutingContext s) {
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        if (pathVariable != null) {
            String param = StringUtils.isBlank(pathVariable.value()) ? parameter.getName() : pathVariable.value();
            items.add(s.pathParam(param));
        }
    }

    @Override
    public String getParameter() {
        return PathVariable.class.getName();
    }


}
