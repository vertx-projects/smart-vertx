package com.smart.vertx.verticle;

import com.google.inject.Singleton;
import com.smart.vertx.annotation.PathVariable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author peng.bo
 * @date 2022/5/24 13:43
 * @desc
 */
@Singleton
public class PathVariableStrategy implements IParamStrategy {
    @Override
    public Single<Object> init(List<Object> items, Parameter parameter, RoutingContext s) {
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        if (pathVariable != null) {
            String param = StringUtils.isBlank(pathVariable.value()) ? parameter.getName() :pathVariable.value();
            items.add(s.pathParam(param));
        }
        return Single.just(true);
    }

    @Override
    public String getParameter() {
        return PathVariable.class.getName();
    }


}
