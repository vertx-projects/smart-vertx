package com.smart.vertx.ebean.util;

import com.google.common.collect.Maps;
import com.smart.vertx.ebean.SFunction;
import com.smart.vertx.ebean.SerializedLambda;
import com.smart.vertx.exception.VertxResCriteriaException;
import lombok.SneakyThrows;

import javax.persistence.Column;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.ebean.util
 * @date 2023/2/16 11:46
 */
public class LambdaUtils {
    private static final Map<String, WeakReference<SerializedLambda>> FUNC_CACHE = Maps.newConcurrentMap();

    @SneakyThrows
    public static String doIt(SFunction<?, ?> f) {
        SerializedLambda lambda = resolve(f);
        String filed = methodToProperty(lambda.getImplMethodName());
        Class<?> aClass = lambda.getInstantiatedType();
        Field field = aClass.getDeclaredField(filed);
        Column column = field.getAnnotation(Column.class);
        String columnName = field.getName();
        if (Objects.nonNull(column)) {
            columnName = column.name();
        }
        return columnName;
    }

    private static String methodToProperty(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else {
            if (!name.startsWith("get") && !name.startsWith("set")) {
                throw new VertxResCriteriaException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
            }

            name = name.substring(3);
        }

        if (name.length() == 1 || name.length() > 1 && !Character.isUpperCase(name.charAt(1))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }

        return name;
    }

    private static <T> SerializedLambda resolve(SFunction<T, ?> func) {
        Class<?> clazz = func.getClass();
        String canonicalName = clazz.getCanonicalName();
        return Optional.ofNullable(FUNC_CACHE.get(canonicalName)).map(WeakReference::get).orElseGet(() -> {
            SerializedLambda lambda = SerializedLambda.resolve(func);
            FUNC_CACHE.put(canonicalName, new WeakReference<>(lambda));
            return lambda;
        });
    }
}
