package com.smart.vertx.pg;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;


/**
 * @author peng.bo
 * @date 2022/5/20 15:54
 * @desc
 */
@ConfigurationProperties(prefix = "vertx.pg")
@Data
@EqualsAndHashCode(callSuper = false)
public class PgProperties extends DruidDataSource {
    private Map<String, DruidDataSource> dataSource;
}
