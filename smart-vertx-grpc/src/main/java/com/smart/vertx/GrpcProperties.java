package com.smart.vertx;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc verticle 基础配置
 */
@Slf4j
@ConfigurationProperties(prefix = "vertx.grpc")
@Data
public class GrpcProperties {
    private String host;

    {
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("get host error,{}", e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    private Integer port = 8888;
    private Integer connectTimeout = 2000;

    public static class Server {
    }

    public static class Client {

    }
}
