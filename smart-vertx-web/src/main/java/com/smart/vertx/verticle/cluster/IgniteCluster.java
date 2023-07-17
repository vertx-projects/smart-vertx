package com.smart.vertx.verticle.cluster;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.smart.vertx.VertxProperties;
import com.smart.vertx.core.ignite.IgniteNode;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.VertxOptions;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheMetrics;
import org.apache.ignite.cluster.ClusterMetrics;
import org.apache.ignite.spi.discovery.tcp.internal.TcpDiscoveryNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.smart.vertx.verticle.SuperVerticle.springContext;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle.cluster
 * @date 2022/7/5 13:16
 */
@Slf4j
@Service
public class IgniteCluster implements ClusterManagerStrategy {
    @Resource
    private CollectorRegistry collectorRegistry;

    @Override
    public void startClusterManager(Router router, VertxProperties props) {
        Single.defer(() -> {
                    VertxOptions vertxOptions = springContext.getBean(VertxOptions.class);
                    return Single.just((IgniteClusterManager) vertxOptions.getClusterManager());
                }).map(clusterManager -> {
                    //集群心跳监测添加
                    router.get("/health/:v").handler(t -> {
                        Ignite ignite = clusterManager.getIgniteInstance();
                        List<IgniteNode> nodes = ignite.cluster().nodes().stream().map(u -> new IgniteNode((TcpDiscoveryNode) u)).collect(Collectors.toList());
                        JSONObject object = new JSONObject();
                        object.put("nodes", nodes);
                        object.put("snapshot", ignite.snapshot());
                        long v = Long.parseLong(t.pathParam("v"));
                        if (Objects.nonNull(ignite
                                .cluster()
                                .topology(v))) {
                            object.put("top", ignite
                                    .cluster()
                                    .topology(v)
                                    .stream()
                                    .map(u -> new IgniteNode((TcpDiscoveryNode) u))
                                    .collect(Collectors.toList()));
                        }
                        object.put("top.version", ignite.cluster().topologyVersion());
                        //ACTIVE(正常)/ACTIVE_READ_ONLY(只读)/INACTIVE(冻结)
                        object.put("status", ignite.cluster().state());
                        object.put("node.count", ignite.cluster().nodes().size());
                        object.put("id", ignite.cluster().id());
                        object.put("tag", ignite.cluster().tag());
                        t.response().end(object.toString());
                        log.info("mapping GET health check {} to vertx success", "/health/:v");
                    });
                    return clusterManager;
                }).doOnSuccess(clusterManager ->
                        new IgniteMetricCollector(clusterManager.getIgniteInstance()).register(collectorRegistry))
                .onErrorComplete(throwable -> {
                    log.debug("not cluster mode ignore start ignite");
                    return true;
                }).subscribe();
    }

    @Override
    public VertxProperties.ClusterType CLUSTER_TYPE() {
        return VertxProperties.ClusterType.ignite;
    }

    static class IgniteMetricCollector extends Collector {
        private final Ignite ignite;

        public IgniteMetricCollector(Ignite ignite) {
            this.ignite = ignite;
        }

        @Override
        public List<MetricFamilySamples> collect() {
            //ignite集群状态指标监控
            GaugeMetricFamily clusterMetrics = new GaugeMetricFamily("cluster_metric",
                    "ignite cluster metric.",
                    Arrays.asList("measure", "cluster_status", "cluster_tag"));
            clusterMetrics.addMetric(List.of("cluster_node_count",
                    ignite.cluster().state().name(), ignite.cluster().tag()), ignite.cluster().nodes().size());
            clusterMetrics.addMetric(List.of("cluster_top_version",
                    ignite.cluster().state().name(), ignite.cluster().tag()), ignite.cluster().topologyVersion());
            //ignite集群缓存指标监控
            GaugeMetricFamily cacheMetrics = new GaugeMetricFamily("cache_metric", "ignite cache metric.", Arrays.asList("measure", "cache"));
            for (String c : ignite.cacheNames()) {
                CacheMetrics metrics = ignite.cache(c).localMetrics();
                cacheMetrics.addMetric(Arrays.asList("get_average_time", c), metrics.getAverageGetTime());
                cacheMetrics.addMetric(Arrays.asList("put_average_time", c), metrics.getAveragePutTime());
                cacheMetrics.addMetric(Arrays.asList("size", c), metrics.getCacheSize());
            }
            //ignite集群节点指标监控
            GaugeMetricFamily nodeMetrics = new GaugeMetricFamily("node_metric",
                    "ignite node metric.",
                    Arrays.asList("measure", "node"));
            ClusterMetrics igniteNodeMetrics = ignite.cluster().localNode().metrics();
            String nodeId = ignite.cluster().localNode().id().toString();
            nodeMetrics.addMetric(Arrays.asList("busy_time", nodeId), igniteNodeMetrics.getBusyTimePercentage());
            nodeMetrics.addMetric(Arrays.asList("idle_time", nodeId), igniteNodeMetrics.getIdleTimePercentage());
            nodeMetrics.addMetric(Arrays.asList("cpu_load", nodeId), igniteNodeMetrics.getCurrentCpuLoad());
            nodeMetrics.addMetric(Arrays.asList("thread_count", nodeId), igniteNodeMetrics.getCurrentThreadCount());
            nodeMetrics.addMetric(Arrays.asList("daemon_thread_count", nodeId), igniteNodeMetrics.getCurrentDaemonThreadCount());
            nodeMetrics.addMetric(Arrays.asList("send_bytes_count", nodeId), igniteNodeMetrics.getSentBytesCount());
            nodeMetrics.addMetric(Arrays.asList("receive_bytes_count", nodeId), igniteNodeMetrics.getReceivedBytesCount());
            nodeMetrics.addMetric(Arrays.asList("heap_memory_used", nodeId), igniteNodeMetrics.getHeapMemoryUsed());
            nodeMetrics.addMetric(Arrays.asList("current_active_jobs", nodeId), igniteNodeMetrics.getCurrentActiveJobs());
            return Lists.newArrayList(nodeMetrics, cacheMetrics, clusterMetrics);
        }
    }
}
