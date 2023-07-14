package com.smart.vertx.core.sign;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.sign
 * @date 2022/7/5 18:43
 */
@Slf4j
@ConfigurationProperties(prefix = "vertx.sign")
@Data
public class SignProperties {
    private String clientKey;
    private String clientSecret;
}
