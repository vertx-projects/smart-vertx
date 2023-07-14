package com.smart.vertx;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.annotation.JSONField;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.ToString;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author peng.bo
 * @date 2022/5/20 18:02
 * @desc
 */
@ConfigurationProperties(prefix = "vertx.kafka.consumer")
@ToString
@Data
public class ConsumerProperties {
    @JSONField(name = "max.poll.records")
    private long maxPollRecords = 1000;
    @JSONField(name = "enable.auto.commit")
    private boolean enableAutoCommit = false;
    @JSONField(name = "auto.offset.reset")
    private String autoOffsetReset = "earliest";
    @JSONField(name = "key.deserializer")
    private Class<?> keyDeserializer = StringDeserializer.class;
    @JSONField(name = "value.deserializer")
    private Class<?> valueDeserializer = StringDeserializer.class;
    @JSONField(name = "group.id")
    private String groupId = "vertx_default";

    @JSONField(serialize = false)
    private final Map<String, String> properties = Maps.newHashMapWithExpectedSize(16);

    public Map<String, String> buildProperties() {
        Map<String, String> consumerMap = JSON.parseObject(JSON.toJSONString(this), new TypeReference<>() {
        });
        if (!properties.isEmpty()) {
            consumerMap.putAll(properties);
        }
        return consumerMap;
    }
}
