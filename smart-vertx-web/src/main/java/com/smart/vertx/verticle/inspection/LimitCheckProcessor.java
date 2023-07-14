package com.smart.vertx.verticle.inspection;

import com.google.common.util.concurrent.RateLimiter;
import com.smart.vertx.core.limit.LimitProperties;
import com.smart.vertx.exception.VertxResCriteriaException;
import com.smart.vertx.util.SpringUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

import static com.smart.vertx.verticle.SuperVerticle.springContext;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle.inspection
 * @date 2022/8/1 16:26
 */
@Service
public class LimitCheckProcessor implements IProcessorChain {
    @Resource
    LimitProperties limitProperties;

    @Override
    public int order() {
        return -1;
    }

    private final RateLimiter rateLimiter;

    public LimitCheckProcessor() {
        this.rateLimiter = SpringUtils.getBean(springContext, RateLimiter.class);
    }

    @Override
    public Single<Boolean> handler(CheckParams request) {
        RoutingContext s = request.getS();
        //limit check
        if (Objects.nonNull(rateLimiter)) {
            boolean acquire = rateLimiter.tryAcquire(limitProperties.getTimeout(), limitProperties.getTimeunit());
            //超时获取不到令牌
            if (!acquire) {
                s.fail(HttpResponseStatus.BAD_GATEWAY.code(), new VertxResCriteriaException(limitProperties.getMsg()));
                return Single.just(false);
            }
            return nextProcessor().handler(request);
        }
        return nextProcessor().handler(request);
    }
}
