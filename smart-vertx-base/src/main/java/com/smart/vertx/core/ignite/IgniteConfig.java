package com.smart.vertx.core.ignite;

import com.google.common.collect.Lists;
import com.smart.vertx.VertxClusterProperties;
import com.smart.vertx.core.ClusterConfig;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;
import io.vertx.spi.cluster.ignite.impl.VertxLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.EventType;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.ingite
 * @date 2022/6/30 18:07
 */
@Slf4j
@Configuration
public class IgniteConfig extends ClusterConfig {
    @Bean
    @ConditionalOnBean(VertxClusterProperties.Maker.class)
    @ConditionalOnProperty(prefix = "vertx", name = {"cluster-type"}, havingValue = "ignite")
    public ClusterManager clusterManager() {
        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
        igniteConfiguration.setIncludeEventTypes(
                EventType.EVT_CLUSTER_SNAPSHOT_FINISHED,
                EventType.EVT_NODE_JOINED,
                EventType.EVT_NODE_LEFT,
                EventType.EVT_NODE_FAILED,
                EventType.EVT_CLUSTER_STATE_CHANGED);
        igniteConfiguration.setDiscoverySpi(getTcpDiscoverySpi());
        igniteConfiguration.setCacheConfiguration(getCacheConfiguration());
        igniteConfiguration.setGridLogger(new VertxLogger());
        igniteConfiguration.setMetricsLogFrequency(0);
        igniteConfiguration.setClientMode(vertxProperties.isClientMode());
        return new IgniteClusterManager(igniteConfiguration);
    }

    public TcpDiscoverySpi getTcpDiscoverySpi() {
        //JDK17 在此位置会有反射异常 java.nio.DirectByteBuffer.address为私有字段无法通过反射获取
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder tcpDiscoveryVmIpFinder = new TcpDiscoveryVmIpFinder();
        try {
            Collection<String> address = getRegisteredAddresses("47500..47599");
            tcpDiscoveryVmIpFinder.setAddresses(address);
            tcpDiscoverySpi.setIpFinder(tcpDiscoveryVmIpFinder);
        } catch (Exception e) {
            log.info("ignite kubernetes endpoints empty use [TcpDiscoveryMulticastIpFinder].");
            tcpDiscoverySpi.setIpFinder(new TcpDiscoveryMulticastIpFinder());
        }
        return tcpDiscoverySpi;
    }


    public CacheConfiguration<String, Object>[] getCacheConfiguration() {
        List<CacheConfiguration<String, Object>> cacheConfigurations = Lists.newArrayList();
        CacheConfiguration<String, Object> commonConfiguration = new CacheConfiguration<>();
        commonConfiguration.setName("*");
        commonConfiguration.setCacheMode(CacheMode.PARTITIONED);
        commonConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        commonConfiguration.setReadFromBackup(false);
        //开启缓存指标监控
        commonConfiguration.setStatisticsEnabled(true);
        commonConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
        commonConfiguration.setBackups(1);
        commonConfiguration.setAffinity(new RendezvousAffinityFunction().setPartitions(128));

        CacheConfiguration<String, Object> vertxConfiguration = new CacheConfiguration<>();
        vertxConfiguration.setName("__vertx.*");
        //开启缓存指标监控
        vertxConfiguration.setStatisticsEnabled(true);
        vertxConfiguration.setCacheMode(CacheMode.PARTITIONED);
        vertxConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        vertxConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);

        cacheConfigurations.add(commonConfiguration);
        cacheConfigurations.add(vertxConfiguration);
        return cacheConfigurations.toArray(new CacheConfiguration[]{});
    }


}
