package com.smart.vertx.verticle.inspection;

import com.smart.vertx.core.OpentracingContext;
import com.smart.vertx.util.SpringUtils;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.smart.vertx.verticle.SuperVerticle.springContext;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle.inspection
 * @date 2022/8/1 15:48
 */
@Service
public class OpenTraceProcessor implements IProcessorChain {
    @Override
    public boolean first() {
        return true;
    }

    @Override
    public int order() {
        return -2;
    }

    private final OpentracingContext opentracingContext;

    public OpenTraceProcessor() {
        this.opentracingContext = SpringUtils.getBean(springContext, OpentracingContext.class);
    }

    @Override
    public Single<Boolean> handler(CheckParams request) {
        RoutingContext s = request.getS();
        //open trace check
        if (Objects.nonNull(opentracingContext)) {
            opentracingContext.request(s, s.request());
        }
        return nextProcessor().handler(request);
    }
}
