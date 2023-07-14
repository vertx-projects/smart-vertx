package com.smart.vertx.core.limit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author peng.bo
 * @date 2022/5/20 15:54
 * @desc
 */
@ConfigurationProperties(prefix = "vertx.limit")
@Data
@EqualsAndHashCode(callSuper = false)
public class LimitProperties {
    /**
     * 限流每秒最多的访问限制次数
     */
    double permitsPerSecond;
    /**
     * 获取令牌最大等待时间
     */
    long timeout = 10;
    /**
     * 预热时间
     */
    long warmupPeriod = 0;
    /**
     * 时间单位
     */
    TimeUnit timeunit = TimeUnit.MILLISECONDS;
    /**
     * 得不到令牌的提示语
     */
    String msg = "系统已限流,请稍后再试.";
}
