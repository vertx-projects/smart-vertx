package com.smart.vertx.verticle.cluster;

import com.smart.vertx.VertxProperties;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.spi.cluster.hazelcast.ClusterHealthCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle.cluster
 * @date 2022/7/5 13:15
 */
@Slf4j
@Service
public class HazelcastCluster implements ClusterManagerStrategy {
    @Resource
    Vertx vertx;

    @Override
    public void startClusterManager(Router router, VertxProperties props) {
        //集群心跳监测添加
        Handler<Promise<Status>> procedure = ClusterHealthCheck.createProcedure(vertx.getDelegate());
        HealthChecks checks = HealthChecks.create(vertx.getDelegate()).register("cluster-health", procedure);
        router.get("/health").getDelegate().handler(HealthCheckHandler.createWithHealthChecks(checks));
        log.info("mapping GET health check {} to vertx success", "/health");
    }

    @Override
    public VertxProperties.ClusterType CLUSTER_TYPE() {
        return VertxProperties.ClusterType.hazelcast;
    }
}
