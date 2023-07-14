package com.smart.vertx.servicediscovery;

import com.google.common.collect.Maps;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;

/**
 * @author peng.bo
 * @date 2022/5/24 11:51
 * @desc
 */
public interface IParamStrategy {

    Map<String, IParamStrategy> strategyMap = Maps.newConcurrentMap();

    Object init(Object data, Parameter parameter, Single<HttpRequest<Buffer>> request);

    //新增获取Type
    String getParameter();

    @PostConstruct
    default void initStrategy() {
        strategyMap.put(getParameter(), this);
    }

    static IParamStrategy get(Parameter parameter) {
        IParamStrategy strategy = strategyMap.get(parameter.getType().getName());
        if (Objects.nonNull(strategy)) {
            return strategy;
        } else {
            Annotation[] annotations = parameter.getAnnotations();
            if (annotations.length > 0) {
                //只取第一个注解作为参数校验注解
                Annotation annotation = annotations[0];
                strategy = strategyMap.get(annotation.annotationType().getName());
                if (Objects.nonNull(strategy)) {
                    return strategy;
                } else {
                    return strategyMap.get(Object.class.getName());
                }
            } else {
                return strategyMap.get(Object.class.getName());
            }
        }
    }
}
