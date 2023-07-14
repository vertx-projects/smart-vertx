package com.smart.vertx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author peng.bo
 * @date 2022/5/20 15:54
 * @desc
 */
@ConfigurationProperties(prefix = "vertx.mongo")
@Data
@EqualsAndHashCode(callSuper = false)
public class MongoProperties {
    private String host;
    private Integer port;
    @JsonProperty("db_name")
    private String dbName;
    private int minPoolSize = 1;
    private int maxPoolSize = 20;
    private String username;
    private String password;
    // Socket Settings
    private long socketTimeoutMS = 2000;
    private long sendBufferSize = 8192;
    private long connectTimeoutMS = 2000;
    private long receiveBufferSize = 8192;
    private long serverSelectionTimeoutMS = 3000;
    private boolean shared;

    @Override
    public String toString() {
        return "MongoProperties{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", dbName='" + dbName + '\'' +
                ", minPoolSize=" + minPoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", username='" + username + '\'' +
                ", password='<hidden>'" +
                ", socketTimeoutMS=" + socketTimeoutMS +
                ", sendBufferSize=" + sendBufferSize +
                ", connectTimeoutMS=" + connectTimeoutMS +
                ", receiveBufferSize=" + receiveBufferSize +
                ", serverSelectionTimeoutMS=" + serverSelectionTimeoutMS +
                ", shared=" + shared +
                '}';
    }
}
