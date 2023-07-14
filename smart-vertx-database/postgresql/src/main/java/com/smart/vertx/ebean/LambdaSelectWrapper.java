package com.smart.vertx.ebean;

import io.ebean.Expression;
import io.ebean.PagedList;
import io.ebean.Query;

import java.util.*;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.ebean
 * @date 2022/12/19 17:05
 */
public class LambdaSelectWrapper<R> {

    private final Query<R> query;

    public LambdaSelectWrapper(List<Expression> queryExpr, Query<R> query) {
        this.query = query;
        queryExpr.forEach(s -> this.query.where().add(s));
    }

    public <T> LambdaUpdateWrapper<R, T> asUpdate() {
        return new LambdaUpdateWrapper<>(query.asUpdate());
    }

    public R findOne() {
        return query.findOne();
    }

    //JPA级联关联的时候注意删除只能通过ID进行关联
    public boolean delete() {
        return query.delete() > 0;
    }

    public int findCount() {
        return query.findCount();
    }

    public boolean exists() {
        return query.exists();
    }

    public List<R> findList() {
        return query.findList();
    }

    public PagedList<R> paging(int pageIndex, int pageSize) {
        query.setFirstRow((pageIndex - 1) * pageSize);
        query.setMaxRows(pageSize);
        return query.findPagedList();
    }

}
