package com.smart.vertx.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.client
 * @date 2022/6/28 10:51
 */
@ConfigurationProperties(prefix = "vertx.web.client")
@Data
public class WebClientProperties {
    private long timeout = 1000;
}
