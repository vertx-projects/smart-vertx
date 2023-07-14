package com.smart.vertx.verticle.inspection;

import com.google.common.collect.Maps;
import io.reactivex.rxjava3.core.Single;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle.inspection
 * @date 2022/8/1 16:03
 */
public interface IProcessorChain {
    AtomicReference<IProcessorChain> PROCESSOR = new AtomicReference<>();
    Map<Integer, IProcessorChain> strategyMap = Maps.newLinkedHashMapWithExpectedSize(16);

    default boolean first() {
        return false;
    }

    default int last() {
        return -999;
    }

    default IProcessorChain nextProcessor() {
        IProcessorChain IProcessorChain = strategyMap.get(order() + 1);
        if (Objects.isNull(IProcessorChain)) {
            return strategyMap.get(last());
        }
        return IProcessorChain;
    }

    int order();

    Single<Boolean> handler(CheckParams request);

    @PostConstruct
    default void init() {
        strategyMap.put(order(), this);
        if (first()) {
            PROCESSOR.set(this);
        }
    }
}
