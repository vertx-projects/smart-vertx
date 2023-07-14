package com.smart.vertx;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc database 自动配置类
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({RedisProperties.class})
public class RedisAutoConfiguration {
}
