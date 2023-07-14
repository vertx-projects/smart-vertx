package com.smart.vertx.servicediscovery;

import com.smart.vertx.annotation.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * @author peng.bo
 * @date 2022/6/2 9:51
 * @desc
 */
@Slf4j
public abstract class EventBusAbstract {
    @Resource
    private Vertx vertx;
    private MessageConsumer<JsonObject> consumer;

    /*
     * 服务绑定
     */
    protected <T, E extends T> void bind(Class<E> aClass, E e) {
        EventBus eventBus = e.getClass().getInterfaces()[0].getAnnotation(EventBus.class);
        this.consumer = new ServiceBinder(vertx.getDelegate()).setAddress(eventBus.address()).register(aClass, e);
        log.info("bind {} to service {} success.", aClass, e);
    }

    /*
     * 服务解绑
     */
    @PreDestroy
    protected void unbind() {
        ServiceBinder binder = new ServiceBinder(vertx.getDelegate());
        binder.unregister(consumer);
    }
}
