package com.smart.vertx.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.auth
 * @date 2022/6/24 12:27
 */
@ConfigurationProperties(prefix = "vertx.auth")
@Data
public class AuthProperties {
    /**
     * SSO HOST
     */
    private String host;
    /**
     * 是否认证，默认开启
     */
    private boolean enable = true;
    /**
     * SSO REALM
     */
    private String realm = "test";
}
