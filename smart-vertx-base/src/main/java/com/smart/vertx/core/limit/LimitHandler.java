package com.smart.vertx.core.limit;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.limit
 * @date 2022/7/28 18:21
 */


public class LimitHandler {
    @Bean
    public RateLimiter around(LimitProperties properties) {
        //平滑突发限流,每秒创建1000个令牌，预热期为1分钟(一分钟后达到设定速率)
        if (properties.getWarmupPeriod() > 0) {
            return RateLimiter.create(properties.getPermitsPerSecond(), properties.getWarmupPeriod(), properties.getTimeunit());
        }
        return RateLimiter.create(properties.getPermitsPerSecond());
    }
}
