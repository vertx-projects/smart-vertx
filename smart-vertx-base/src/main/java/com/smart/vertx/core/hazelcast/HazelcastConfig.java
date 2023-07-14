package com.smart.vertx.core.hazelcast;

import com.hazelcast.config.*;
import com.smart.vertx.VertxClusterProperties;
import com.smart.vertx.core.ClusterConfig;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.monitor.hazelcast
 * @date 2022/7/5 10:39
 */
@Configuration
@Slf4j
public class HazelcastConfig extends ClusterConfig {

    @Bean
    @ConditionalOnProperty(prefix = "vertx", name = {"cluster-type"}, havingValue = "hazelcast")
    @ConditionalOnBean(VertxClusterProperties.Maker.class)
    public ClusterManager clusterManager() {
        Config cfg = ConfigUtil.loadConfig();
        cfg.setLiteMember(vertxProperties.isClientMode());
        NetworkConfig network = cfg.getNetworkConfig();
        JoinConfig join = network.getJoin();
        try {
            TcpIpConfig tcpipConfig = join.getTcpIpConfig();
            List<String> items = getRegisteredAddresses(NetworkConfig.DEFAULT_PORT + "");
            tcpipConfig.setEnabled(true).setMembers(items);
            //关闭多播
            join.getMulticastConfig().setEnabled(false);
        } catch (Exception e) {
            //打开多播
            log.info("hazelcast kubernetes endpoints empty use [Multicast].");
            join.getMulticastConfig().setEnabled(true);
        }
        return new HazelcastClusterManager(cfg);
    }
}
