package com.smart.vertx.verticle.request;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Maps;
import com.smart.vertx.entity.command.BaseCommand;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.PostConstruct;
import javax.validation.Validation;
import javax.validation.Validator;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author peng.bo
 * @date 2022/5/24 11:51
 * @desc
 */
public interface IParamStrategy {
    void init(List<Object> items, Parameter parameter, RoutingContext s);

    Map<String, IParamStrategy> STRATEGY_MAP = Maps.newConcurrentMap();

    Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    default Object getDefaultValue(Class<?> clazz, String value, RoutingContext s) {
        if (Map.class.isAssignableFrom(clazz)) {
            return buildMap(s);
        } else if (BaseCommand.class.isAssignableFrom(clazz)) {
            return JSON.to(clazz, buildMap(s));
        } else if (StringUtils.isBlank(value)) {
            return null;
        } else if (clazz.equals(int.class)) {
            return Integer.valueOf(value);
        } else if (clazz.equals(byte.class)) {
            return Byte.valueOf(value);
        } else if (clazz.equals(long.class)) {
            return Long.valueOf(value);
        } else if (clazz.equals(double.class)) {
            return Double.valueOf(value);
        } else if (clazz.equals(float.class)) {
            return Float.valueOf(value);
        } else if (clazz.equals(char.class)) {
            return value.toCharArray();
        } else if (clazz.equals(short.class)) {
            return Short.valueOf(value);
        } else if (clazz.equals(boolean.class)) {
            return Boolean.valueOf(value);
        } else if (clazz.isArray()) {
            return toArray(clazz.getComponentType(), value);
        } else {
            return value;
        }
    }

    @NotNull
    private Map<String, Object> buildMap(RoutingContext s) {
        MultiMap multiMap = s.request().params();
        Map<String, Object> map = Maps.newConcurrentMap();
        for (Map.Entry<String, String> entry : multiMap) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    default Object toArray(Class<?> clazz, String source) {
        String[] arrays = source.startsWith("[") ? source.substring(1, source.length() - 1).split(",") : source.split(",");
        if (clazz.equals(String.class)) {
            return arrays;
        } else if (clazz.equals(int.class)) {
            return Arrays.stream(arrays).mapToInt(Integer::parseInt).toArray();
        } else if (clazz.equals(double.class)) {
            return Arrays.stream(arrays).mapToDouble(Double::parseDouble).toArray();
        } else if (clazz.equals(long.class)) {
            return Arrays.stream(arrays).mapToLong(Long::parseLong).toArray();
        }
        return Arrays.stream(arrays).toArray();
    }

    //新增获取Type
    String getParameter();

    @PostConstruct
    default void initStrategy() {
        STRATEGY_MAP.put(getParameter(), this);
    }

    static IParamStrategy get(Parameter parameter) {
        IParamStrategy strategy = STRATEGY_MAP.get(parameter.getType().getName());
        if (Objects.nonNull(strategy)) {
            return strategy;
        } else {
            Annotation[] annotations = parameter.getAnnotations();
            if (annotations.length > 0) {
                //只取第一个注解作为参数校验注解
                Annotation annotation = annotations[0];
                strategy = IParamStrategy.STRATEGY_MAP.get(annotation.annotationType().getName());
                if (Objects.nonNull(strategy)) {
                    return strategy;
                } else {
                    return null;
                }
            } else {
                return STRATEGY_MAP.get(Object.class.getName());
            }
        }
    }
}