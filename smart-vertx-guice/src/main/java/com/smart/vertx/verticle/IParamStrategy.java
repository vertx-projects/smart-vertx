package com.smart.vertx.verticle;

import com.google.common.collect.Maps;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.ext.web.RoutingContext;

import javax.validation.Validation;
import javax.validation.Validator;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

/**
 * @author peng.bo
 * @date 2022/5/24 11:51
 * @desc
 */
public interface IParamStrategy {
    Single<Object> init(List<Object> items, Parameter parameter, RoutingContext s);
    Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    default Object getDefaultValue(Class<?> className, String value, RoutingContext s) {
        if (className.equals(int.class)) {
            return Integer.valueOf(value);
        } else if (className.equals(byte.class)) {
            return Byte.valueOf(value);
        } else if (className.equals(long.class)) {
            return Long.valueOf(value);
        } else if (className.equals(double.class)) {
            return Double.valueOf(value);
        } else if (className.equals(float.class)) {
            return Float.valueOf(value);
        } else if (className.equals(char.class)) {
            return value.toCharArray();
        } else if (className.equals(short.class)) {
            return Short.valueOf(value);
        } else if (className.equals(boolean.class)) {
            return Boolean.valueOf(value);
        } else if (className.equals(Map.class)) {
            MultiMap multiMap = s.request().params();
            Map<String, Object> map = Maps.newConcurrentMap();
            for (Map.Entry<String, String> entry : multiMap) {
                map.put(entry.getKey(), entry.getValue());
            }
            return map;
        } else {
            return value;
        }
    }

    //新增获取Type
    String getParameter();
}
