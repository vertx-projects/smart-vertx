package com.smart.vertx.verticle;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author peng.bo
 * @date 2022/6/7 14:47
 * @desc
 */
@Singleton
public class HttpVerticleFactory {
    @Inject
    Set<IParamStrategy> iParamStrategies;

    public IParamStrategy get(Parameter parameter) {
        Stream<IParamStrategy> stream = iParamStrategies.stream();
        Optional<IParamStrategy> strategy = stream.filter(s -> s.getParameter().equals(parameter.getType().getName())).findFirst();
        if (strategy.isPresent()) {
            return strategy.get();
        } else {
            Annotation[] annotations = parameter.getAnnotations();
            stream = iParamStrategies.stream();
            if (annotations.length > 0) {
                //只取第一个注解作为参数校验注解
                Annotation annotation = annotations[0];
                strategy = stream.filter(s -> s.getParameter().equals(annotation.annotationType().getName())).findFirst();
                return strategy.orElse(null);
            } else {
                return stream.filter(s -> s.getParameter().equals(Object.class.getName())).findFirst().orElse(null);
            }
        }
    }
}
