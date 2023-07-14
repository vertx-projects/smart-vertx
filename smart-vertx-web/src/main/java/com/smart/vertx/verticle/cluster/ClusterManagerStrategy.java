package com.smart.vertx.verticle.cluster;

import com.google.common.collect.Maps;
import com.smart.vertx.VertxProperties;
import io.vertx.rxjava3.ext.web.Router;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle.cluster
 * @date 2022/7/5 13:13
 */
public interface ClusterManagerStrategy {
    Map<VertxProperties.ClusterType, ClusterManagerStrategy> maps = Maps.newConcurrentMap();

    @PostConstruct
    default void init() {
        maps.put(CLUSTER_TYPE(), this);
    }

    static ClusterManagerStrategy get(VertxProperties.ClusterType type) {
        return maps.get(type);
    }

    void startClusterManager(Router router, VertxProperties props);

    VertxProperties.ClusterType CLUSTER_TYPE();
}
