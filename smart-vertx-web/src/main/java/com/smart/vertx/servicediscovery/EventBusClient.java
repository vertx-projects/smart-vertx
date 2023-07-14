package com.smart.vertx.servicediscovery;

import com.smart.vertx.annotation.EventBus;
import com.smart.vertx.enums.CommonConstEnums;
import com.smart.vertx.exception.VertxResCriteriaException;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.rxjava3.core.shareddata.SharedData;
import io.vertx.rxjava3.servicediscovery.ServiceDiscovery;
import io.vertx.rxjava3.servicediscovery.ServiceReference;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;

/**
 * @author peng.bo
 * @date 2022/6/2 13:41
 * @desc
 */
@Slf4j
public abstract class EventBusClient<T> {
    @Resource
    private ServiceDiscovery discovery;
    @Resource
    SharedData sharedData;
    //缓存服务
    private T t;

    protected Single<T> getProxy() {
        if (Objects.isNull(t)) {
            return sharedData.getLocalLock("eventbus_client").flatMap(s -> {
                if (Objects.isNull(t)) {
                    Class<T> tClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                    EventBus eventBus = tClass.getAnnotation(EventBus.class);
                    JsonObject jsonObject = new JsonObject().put(CommonConstEnums.name.name(), eventBus.serviceName().concat(CommonConstEnums._eventbus.name()));
                    return discovery.getRecord(jsonObject)
                            .switchIfEmpty(Single.error(new VertxResCriteriaException("event bus server is not started.")))
                            .map(record -> {
                                ServiceReference reference = discovery.getReferenceWithConfiguration(record, new JsonObject().put(CommonConstEnums.timeout.name(), eventBus.timeOut()).put(CommonConstEnums.tracingPolicy.name(), TracingPolicy.ALWAYS));
                                t = reference.getAs(tClass);
                                return t;
                            }).doFinally(s::release);
                }
                s.release();
                return Single.just(t);
            });
        }
        return Single.just(t);
    }
}
