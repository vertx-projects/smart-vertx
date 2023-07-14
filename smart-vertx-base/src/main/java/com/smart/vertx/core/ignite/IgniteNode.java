package com.smart.vertx.core.ignite;

import lombok.Data;
import org.apache.ignite.lang.IgniteProductVersion;
import org.apache.ignite.spi.discovery.tcp.internal.TcpDiscoveryNode;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.UUID;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.monitor.ignite
 * @date 2022/7/1 13:35
 */
@Data
public class IgniteNode {
    public IgniteNode() {
    }

    public IgniteNode(TcpDiscoveryNode tcpDiscoveryNode) {
        this.id = tcpDiscoveryNode.id();
        this.consistentId = tcpDiscoveryNode.consistentId();
        this.addrs = tcpDiscoveryNode.addresses();
        this.hostNames = tcpDiscoveryNode.hostNames();
        this.discPort = tcpDiscoveryNode.discoveryPort();
        this.lastUpdateTimeNanos = tcpDiscoveryNode.lastUpdateTimeNanos();
        this.lastExchangeTimeNanos = tcpDiscoveryNode.lastExchangeTimeNanos();
        this.visible = tcpDiscoveryNode.visible();
        this.local = tcpDiscoveryNode.isLocal();
        this.version = tcpDiscoveryNode.version();
        this.clientRouterNodeId = tcpDiscoveryNode.clientRouterNodeId();
        this.lastSuccessfulAddr = tcpDiscoveryNode.lastSuccessfulAddress();
        this.daemon = tcpDiscoveryNode.isDaemon();
        this.client = tcpDiscoveryNode.isClient();
        this.clientAlive = tcpDiscoveryNode.isClientAlive();
    }

    private volatile UUID id;
    private Object consistentId;
    private Collection<String> addrs;
    private Collection<String> hostNames;
    private int discPort;
    private long lastUpdateTimeNanos;
    private long lastExchangeTimeNanos;
    private boolean visible;
    private boolean local;
    private IgniteProductVersion version;
    private UUID clientRouterNodeId;
    private InetSocketAddress lastSuccessfulAddr;
    private boolean daemon;

    private boolean client;

    private boolean clientAlive;
}
