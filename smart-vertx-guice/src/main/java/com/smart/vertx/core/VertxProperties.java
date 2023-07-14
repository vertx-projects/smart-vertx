package com.smart.vertx.core;

import com.alibaba.fastjson2.annotation.JSONField;
import com.google.inject.Singleton;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author peng.bo
 * @date 2022/5/18 9:56
 * @desc verticle 基础配置
 */
@Slf4j
@Data
@Singleton
public class VertxProperties {

    private int port = 8080;
    /**
     * NIO线程数, 默认为CPU逻辑核心数量
     */
    @JSONField(name = "nio-thread-count")
    private int nioThreadCount = Runtime.getRuntime().availableProcessors();
    /**
     * worker线程池大小
     */
    @JSONField(name = "worker-thread-count")
    private int workerThreadCount = Runtime.getRuntime().availableProcessors();
    /**
     * 部署的http verticle数量
     */
    @JSONField(name = "verticle-count")
    private int verticleCount = Runtime.getRuntime().availableProcessors();
    /**
     * NIO线程处理一个事件允许消耗的最长时间, 毫秒
     */
    @JSONField(name = "worker-thread-count")
    private long maxEventLoopExecuteTime = 2000;
    /**
     * 应用名称
     */
    @JSONField(name = "service-name")
    private String serviceName = "default";
    /**
     * API版本
     */
    @JSONField(name = "api-version")
    private String apiVersion = "v1";
    /**
     * controller包名
     */
    @JSONField(name = "package-name")
    private String packageName = "com.smart.*";
    private String host;

    {
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}
