package com.smart.vertx;

import io.vertx.core.net.JksOptions;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


/**
 * @author peng.bo
 * @date 2022/5/19 13:28
 * @desc
 */
@Component
@ConfigurationProperties(prefix = "vertx.cluster")
@Data
public class VertxClusterProperties {
    private boolean ssl;
    private JksOptions keyStore;
    private JksOptions trustStore;


    @Bean
    public Maker maker() {
        return new Maker();
    }

    public static class Maker {
    }
}
