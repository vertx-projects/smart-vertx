package com.smart.vertx;

import io.vertx.redis.client.RedisOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author peng.bo
 * @date 2022/5/20 15:54
 * @desc
 */
@ConfigurationProperties(prefix = "vertx.redis")
public class RedisProperties extends RedisOptions {
}
