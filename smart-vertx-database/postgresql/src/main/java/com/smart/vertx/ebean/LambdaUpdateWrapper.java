package com.smart.vertx.ebean;

import io.ebean.UpdateQuery;

import static com.smart.vertx.ebean.util.LambdaUtils.doIt;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.ebean
 * @date 2022/12/19 17:05
 */
public class LambdaUpdateWrapper<R, T> {

    private UpdateQuery<R> query;

    public LambdaUpdateWrapper(UpdateQuery<R> query) {
        this.query = query;
    }

    public LambdaUpdateWrapper<R, T> set(SFunction<R, T> f, T value) {
        String columnName = doIt(f);
        query = query.set(columnName, value);
        return this;
    }

    public boolean update() {
        return query.update() > 0;
    }

}
