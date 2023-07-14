package com.smart.vertx;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.annotation.JSONField;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.ToString;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author peng.bo
 * @date 2022/5/20 18:02
 * @desc
 */
@ConfigurationProperties(prefix = "vertx.kafka.producer")
@ToString
@Data
public class ProducerProperties {
    private int retries = 3;
    @JSONField(name = "batch.size")
    private long batchSize = 16384;
    @JSONField(name = "buffer.memory")
    private long bufferMemory = 33554432;
    private String acks = "1";
    @JSONField(name = "key.serializer")
    private Class<?> keySerializer = StringSerializer.class;
    @JSONField(name = "value.serializer")
    private Class<?> valueSerializer = StringSerializer.class;
    @JSONField(serialize = false)
    private final Map<String, String> properties = Maps.newHashMapWithExpectedSize(16);

    public Map<String, String> buildProperties() {
        Map<String, String> map = JSON.parseObject(JSON.toJSONString(this), new TypeReference<>() {
        });
        if (!properties.isEmpty()) {
            map.putAll(properties);
        }
        return map;
    }
}
