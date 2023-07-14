package com.smart.vertx;

import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.redis.client.Redis;
import io.vertx.rxjava3.redis.client.RedisAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author peng.bo
 * @date 2022/5/20 16:04
 * @desc
 */
@Slf4j
public class RedisHandler {
    @Bean
    @ConditionalOnMissingBean
    public RedisAPI redisClient(Vertx vertx, RedisProperties redisProperties) {
        log.info("redis client is started .{}", redisProperties.toJson());
        return RedisAPI.api(Redis.createClient(vertx, redisProperties));
    }
}
