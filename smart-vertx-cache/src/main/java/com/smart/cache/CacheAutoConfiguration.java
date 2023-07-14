package com.smart.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.cache
 * @date 2022/9/14 15:57
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({CacheProperties.class})
@EnableCaching
public class CacheAutoConfiguration {

    @Bean
    public CacheManager caffeineCacheManager(CacheProperties cacheProperties) {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                //初始大小
                .initialCapacity(cacheProperties.getInitialCapacity())
                //最大大小
                .maximumSize(cacheProperties.getMaximumSize())
                //写入/更新之后过期
                .expireAfterWrite(cacheProperties.getDuration(), TimeUnit.MINUTES)
                //写后自动异步刷新
                .refreshAfterWrite(cacheProperties.getDuration(), TimeUnit.MINUTES)
                //记录下缓存的一些统计数据，例如命中率等
                .recordStats();
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setAllowNullValues(true);
        caffeineCacheManager.setCaffeine(caffeine);
        caffeineCacheManager.setCacheLoader(new CacheLoader<>() {
            @Override
            public Object load(@NonNull Object o) {
                return null;
            }

            //将oldValue值返回回去，进而刷新缓存
            @Override
            public Object reload(@NonNull Object key, @NonNull Object oldValue) {
                return oldValue;
            }
        });
        return caffeineCacheManager;
    }

    @Bean
    public Cache<String, Object> cache(CacheProperties cacheProperties) {
        /*Caffeine的缓存清除是惰性的，可能发生在读请求后或者写请求后
        比如说有一条数据过期后，不会立即删除，可能在下一次读/写操作后触发删除（类比于redis的惰性删除）。*/
        return Caffeine.newBuilder()
                .initialCapacity(cacheProperties.getInitialCapacity())
                .maximumSize(cacheProperties.getMaximumSize())
                //写入/更新之后过期
                .expireAfterWrite(cacheProperties.getDuration(), TimeUnit.MINUTES)
                //写后自动异步刷新
                .refreshAfterWrite(cacheProperties.getDuration(), TimeUnit.MINUTES)
                .build();
    }
}
