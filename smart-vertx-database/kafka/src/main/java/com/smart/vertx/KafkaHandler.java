package com.smart.vertx;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

import java.util.Map;

/**
 * @author peng.bo
 * @date 2022/5/20 19:18
 * @desc
 */
@Slf4j
public class KafkaHandler {

    @Bean
    public Map<String, String> kafka(KafkaProperties kafkaProperties) {
        return JSON.parseObject(JSON.toJSONString(kafkaProperties), new TypeReference<>() {
        });
    }

    @Bean
    public Map<String, String> kafkaProducer(Map<String, String> kafka, ProducerProperties producerProperties) {
        Map<String, String> producerMap = producerProperties.buildProperties();
        kafka.putAll(producerMap);
        log.info("kafka producer property is init .{}", kafka);
        return kafka;
    }

    @Bean
    public Map<String, String> kafkaConsumer(Map<String, String> kafka, ConsumerProperties consumerProperties) {
        Map<String, String> consumerMap = consumerProperties.buildProperties();
        kafka.putAll(consumerMap);
        log.info("kafka consumer property is init .{}", kafka);
        return kafka;
    }
}
