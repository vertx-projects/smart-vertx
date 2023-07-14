package com.smart.vertx.ebean;

import io.ebean.event.BeanPersistAdapter;
import io.ebean.event.BeanPersistRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;

/*@EbeanComponent*/
@Slf4j
public class BasePersistAdapter<T> extends BeanPersistAdapter {

    @Override
    public boolean isRegisterFor(Class<?> cls) {
        Class<T> tClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return tClass.equals(cls);
    }

    @Override
    public void postInsert(BeanPersistRequest<?> request) {
        final Object bean = request.bean();
        log.info("post insert {}.", bean);
    }

    @Override
    public void postUpdate(BeanPersistRequest<?> request) {
        final Object bean = request.bean();
        log.info("post update [{}] updated:[{}] dirty:[{}].", bean, request.updatedProperties(), request.updatedValues());
    }
}
