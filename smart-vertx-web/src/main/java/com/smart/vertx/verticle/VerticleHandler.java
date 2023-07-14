package com.smart.vertx.verticle;

import ch.qos.logback.core.util.Loader;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.smart.vertx.annotation.EventBus;
import com.smart.vertx.annotation.RequestMapping;
import com.smart.vertx.annotation.RestController;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author peng.bo
 * @date 2022/5/18 10:39
 * @desc 装载全部handler
 */
@Slf4j
@ComponentScan({"com.smart.vertx.verticle"})
public class VerticleHandler implements ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    public Single<Map<String, List<Tuple>>> buildData(String packageName) {
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);
        Map<String, List<Tuple>> maps = Maps.newHashMapWithExpectedSize(16);
        try {
            Resource[] resources = resolver.getResources("classpath*:".concat(packageName));
            for (Resource resource : resources) {
                MetadataReader reader = metaReader.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                Class<?> cla = Loader.loadClass(className);
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
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return Single.just(maps);
    }

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
