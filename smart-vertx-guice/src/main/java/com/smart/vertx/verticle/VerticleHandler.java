package com.smart.vertx.verticle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.smart.vertx.annotation.EventBus;
import com.smart.vertx.annotation.RequestMapping;
import com.smart.vertx.annotation.RestController;
import com.smart.vertx.utils.CommonUtil;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author peng.bo
 * @date 2022/5/18 10:39
 * @desc 装载全部handler
 */
@Slf4j
@Singleton
public class VerticleHandler {
    public Map<String, List<Tuple>> buildHandler(String packageName) {
        Set<Class<?>> classes = CommonUtil.getClasses(packageName);
        Map<String, List<Tuple>> maps = Maps.newHashMapWithExpectedSize(16);
        for (Class<?> cla : classes) {
            RestController restController = cla.getAnnotation(RestController.class);
            if (restController != null) {
                String path = restController.value();
                Method[] ms = cla.getDeclaredMethods();
                List<Tuple> items = Lists.newArrayListWithExpectedSize(16);
                for (Method m : ms) {
                    RequestMapping mapping = m.getAnnotation(RequestMapping.class);
                    if (mapping != null) {
                        items.add(Tuple.of(mapping, m, cla));
                    }
                }
                maps.put(path, items);
            }
            EventBus eventBus = cla.getAnnotation(EventBus.class);
            if (Objects.nonNull(eventBus)) {
                SuperVerticle.classes.add(cla);
            }
        }
        return maps;
    }

}
