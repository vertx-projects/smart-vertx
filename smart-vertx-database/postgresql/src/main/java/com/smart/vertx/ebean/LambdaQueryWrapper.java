package com.smart.vertx.ebean;

import io.ebean.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.smart.vertx.ebean.util.LambdaUtils.doIt;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.ebean
 * @date 2022/12/19 17:05
 */
public class LambdaQueryWrapper<R> {
    private final List<Expression> queryExpr;
    private final Query<R> query;


    public LambdaQueryWrapper(List<Expression> queryExpr, Query<R> query) {
        this.queryExpr = queryExpr;
        this.query = query;
    }

    public Query<R> getQuery() {
        queryExpr.forEach(s -> this.query.where().add(s));
        return query;
    }

    public <T> LambdaQueryWrapper<R> eq(SFunction<R, T> f, T value) {
        String columnName = doIt(f);
        queryExpr.add(Expr.eq(columnName, value));
        return this;
    }

    public <T> LambdaQueryWrapper<R> rowSql(String sql) {
        query.setRawSql(RawSqlBuilder.parse(sql).create());
        return this;
    }

    public <T> LambdaQueryWrapper<R> gt(SFunction<R, T> f, T value) {
        String columnName = doIt(f);
        queryExpr.add(Expr.gt(columnName, value));
        return this;
    }


    public LambdaQueryWrapper<R> select(String... columns) {
        query.select(StringUtils.join(columns, ","));
        return this;
    }

    //fetch("customer","name") customer 表名，name 表字段
    public LambdaQueryWrapper<R> fetch(String table, String... columns) {
        query.fetch(table, StringUtils.join(columns, ","));
        return this;
    }

    public <T> LambdaQueryWrapper<R> havingGt(String condition, T value) {
        query.having(Expr.gt(condition, value));
        return this;
    }

    public <T> LambdaQueryWrapper<R> havingLt(String condition, T value) {
        query.having(Expr.lt(condition, value));
        return this;
    }

    public <T> LambdaQueryWrapper<R> havingEq(String condition, T value) {
        query.having(Expr.eq(condition, value));
        return this;
    }

    public LambdaSelectWrapper<R> end() {
        return new LambdaSelectWrapper<>(queryExpr, query);
    }

    public <T> DtoQuery<T> asDto(Class<T> dtoClass) {
        queryExpr.forEach(s -> this.query.where().add(s));
        return query.asDto(dtoClass);
    }
}
