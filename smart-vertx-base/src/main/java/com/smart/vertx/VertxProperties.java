package com.smart.vertx;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc verticle 基础配置
 */
@Slf4j
@ConfigurationProperties(prefix = "vertx")
@Data
public class VertxProperties {
    /**
     * NIO线程数, 默认为CPU逻辑核心数量
     */
    private int nioThreadCount = Runtime.getRuntime().availableProcessors();
    /**
     * worker线程池大小
     */
    private int workerThreadCount = Runtime.getRuntime().availableProcessors();
    /**
     * 部署的http verticle数量
     */
    private int verticleCount = Runtime.getRuntime().availableProcessors();
    /**
     * controller包名
     */
    private String packageName = "com/smart/**/*.class";
    /**
     * NIO线程处理一个事件允许消耗的最长时间, 毫秒
     */
    private long maxEventLoopExecuteTime = 2000;
    /**
     * 应用名称
     */
    private String serviceName = "default";
    /**
     * API版本
     */
    private String apiVersion = "v1";
    /**
     * 服务部署标签
     */
    private String label = "cluster";
    /**
     * ignite 模式
     */
    private boolean clientMode = false;
    /**
     * 开启指标收集
     */
    private boolean metrics = false;
    /**
     * 服务部署命名空间
     */
    private String namespace;

    /**
     * 集群模式，默认：hazelcast
     */
    private ClusterType clusterType = ClusterType.hazelcast;

    /**
     * 主机HOST
     */
    private String host;

    {
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("get host error,{}", e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 集群模式
     */
    public enum ClusterType {
        /**
         * hazelcast
         */
        hazelcast,
        /**
         * ignite
         */
        ignite
    }
}
