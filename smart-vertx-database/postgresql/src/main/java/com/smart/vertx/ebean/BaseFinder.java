package com.smart.vertx.ebean;

import com.google.common.collect.Lists;
import io.ebean.Expression;
import io.ebean.Finder;
import io.ebean.Query;

import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.ebean
 * @date 2022/9/23 16:44
 */
public class BaseFinder<T, I> {
    protected smartFinder<T, I> finder;

    public BaseFinder() {
        finder = new smartFinder<>((Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    public static class smartFinder<R, I> extends Finder<I, R> {

        private final Class<R> clazz;

        public smartFinder(Class<R> type) {
            super(type);
            clazz = type;
        }

        public LambdaQueryWrapper<R> lambda() {
            List<Expression> queryExpr = Lists.newArrayListWithExpectedSize(16);
            Query<R> query = db().find(clazz);
            return new LambdaQueryWrapper<>(queryExpr, query);
        }
    }
}
