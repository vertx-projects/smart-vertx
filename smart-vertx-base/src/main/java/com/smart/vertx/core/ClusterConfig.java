package com.smart.vertx.core;

import com.google.common.collect.Lists;
import com.smart.vertx.VertxProperties;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.monitor
 * @date 2022/7/5 11:24
 */
@Slf4j
public class ClusterConfig {
    @Resource
    protected VertxProperties vertxProperties;

    protected List<String> getRegisteredAddresses(String port) {
        Config config = Config.autoConfigure();
        config.setTrustCerts(true);
        List<String> items = Lists.newArrayList();
        //构建client
        try (DefaultKubernetesClient client = new DefaultKubernetesClient(config)) {
            // 获取所有pod
            List<Pod> pods = client.pods()
                    .inNamespace(vertxProperties.getNamespace())
                    .withLabel(vertxProperties.getLabel())
                    .list()
                    .getItems();
            pods.forEach(s -> {
                log.info("pod add in cluster.{}", s.getStatus().toString());
                items.add(s.getStatus().getPodIP().concat(":" + port));
            });
        }
        return items;
    }
}
