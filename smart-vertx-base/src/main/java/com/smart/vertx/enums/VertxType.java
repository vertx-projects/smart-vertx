package com.smart.vertx.enums;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.icos.vertx.enums
 * @date 2023/7/17 11:17
 */
public enum VertxType {
    /**
     * This option is used when running Vert.x in a clustered mode. In this mode, multiple Vert.x instances can form a cluster and communicate with each other to provide high availability and fault tolerance. It allows for distributed computing and sharing of workloads among the cluster members.
     */
    VERTX_CLUSTER,
    /**
     * This option is used when running Vert.x in a standalone mode. In this mode, Vert.x runs as a single instance without clustering or distributed computing capabilities. It is suitable for simple applications or when clustering is not required.
     * VERTX_WORK: This option is used when running Vert.x in a worker mode. In this mode,
     */
    VERTX_STANDALONE,
    /**
     * This option is used when running Vert.x in a worker mode. In this mode, Vert.x instances can be deployed as worker verticles, which are dedicated to executing blocking or long-running tasks. Worker verticles are isolated from the event loop and can be used to perform CPU-intensive operations without blocking the main event loop, ensuring the responsiveness of the application.
     */
    VERTX_WORK
}
