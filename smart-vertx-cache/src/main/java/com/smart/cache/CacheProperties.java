package com.smart.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.cache
 * @date 2022/9/14 16:00
 */
@ConfigurationProperties(prefix = "vertx.cache")
@Data
public class CacheProperties {
    /**
     * 最大大小
     */
    private long maximumSize = 200;
    /**
     * 初始大小
     */
    private int initialCapacity = 20;
    /**
     * 缓存过期时长，单位：分钟
     */
    private long duration = 10;
}
