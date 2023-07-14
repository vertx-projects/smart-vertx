package com.smart.vertx.selector;

import com.smart.vertx.MongoProperties;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.selector
 * @date 2023/7/13 17:39
 */
@Slf4j
public class MongoHandler {
    @Bean
    @ConditionalOnMissingBean
    public MongoClient mongoClient(Vertx vertx, MongoProperties mongoProperties) {
        log.info("mongo cluster is staring .{}", mongoProperties);
        MongoClient client;
        if (mongoProperties.isShared()) {
            client = MongoClient.createShared(vertx, JsonObject.mapFrom(mongoProperties));
            log.info("shared mongo cluster is started.");
        } else {
            client = MongoClient.create(vertx, JsonObject.mapFrom(mongoProperties));
            log.info("mongo cluster is started.");
        }
        return client;
    }
}
