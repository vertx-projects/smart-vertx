package com.smart.vertx.core.sign;

/**
 * 需要认证的接口实现改接口返回对应 clientSecret
 *
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.core.sign
 * @date 2022/7/12 17:51
 */
public interface ClientSecretHandler {
    String clientSecret = "default";

    default String getClientSecret(String clientKey) {
        return clientSecret;
    }
}
