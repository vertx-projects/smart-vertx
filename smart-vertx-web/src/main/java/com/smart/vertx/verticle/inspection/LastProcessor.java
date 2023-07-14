package com.smart.vertx.verticle.inspection;

import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle.inspection
 * @date 2022/12/8 17:17
 */
@Slf4j
@Service
public class LastProcessor implements IProcessorChain {
    @Override
    public int order() {
        return last();
    }

    @Override
    public Single<Boolean> handler(CheckParams request) {
        log.info("processor chain process success.");
        return Single.just(true);
    }
}
