package com.smart.vertx.handler;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author peng.bo
 * @date 2022/5/19 13:28
 * @desc
 */
@Slf4j
@ConfigurationProperties(prefix = "vertx.tcp")
@Data
public class TcpProperties {
    private String host;

    {
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("get host error,{}", e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    private Integer port = 7777;
    private Integer connectTimeout = 2000;


    public static class Server {
    }

    public static class Client {
    }
}
