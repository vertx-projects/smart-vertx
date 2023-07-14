package com.smart.vertx;

import io.vertx.cassandra.CassandraClientOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * @author peng.bo
 * @date 2022/5/20 15:54
 * @desc
 */
@ConfigurationProperties(prefix = "vertx.cassandra")
@Data
@EqualsAndHashCode(callSuper = false)
public class CassandraProperties extends CassandraClientOptions {
    private Map<String, Integer> servers;
    private boolean shared;
    private String sharedClientName;
}
