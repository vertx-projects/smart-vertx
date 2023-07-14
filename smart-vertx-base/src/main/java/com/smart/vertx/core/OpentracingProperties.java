package com.smart.vertx.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author pengbo
 * @version V1.0
 * @Package PACKAGE_NAME
 * @date 2022/6/21 16:15
 */
@Slf4j
@ConfigurationProperties(prefix = "vertx.opentracing")
@Data
public class OpentracingProperties {
    /**
     * Jaeger host
     */
    private String host = "localhost";
    /**
     * Jaeger port
     */
    private Integer port = 80;
    /**
     * 0到1
     */
    private Integer constSampler = 1;
    /**
     * 采样队列大小
     */
    private Integer queenSize = 10000;
    /**
     * flush时间间隔
     */
    private Integer flushIntervalMs = 100;
    /**
     * 开启链路追踪
     */
    private boolean enable;
}
