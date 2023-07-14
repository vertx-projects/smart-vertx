package com.smart.vertx;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author peng.bo
 * @date 2022/5/20 15:54
 * @desc
 */
@ConfigurationProperties(prefix = "vertx.kafka")
@ToString
@Data
public class KafkaProperties {
    @JSONField(name = "bootstrap.servers")
    private String bootstrapServers = "localhost:9092";
    @JSONField(name = "schema.registry.url")
    private String schemaRegistryUrl;
}
