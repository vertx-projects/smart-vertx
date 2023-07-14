package com.smart.vertx.messagecodes.model;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author peng.bo
 * @date 2022/5/20 12:05
 * @desc
 */
public class ProtoCommonMsg {
    private final Map<Object, Object> data = Maps.newHashMap();

    public void put(Object key, Object value) {
        data.put(key, value);
    }

    public Object get(Object key) {
        return data.get(key);
    }

    public <T> T get(Object key, Class<T> clazz) {

        return (T) data.get(key);
    }

    public String toString() {
        return data.toString();
    }
}
