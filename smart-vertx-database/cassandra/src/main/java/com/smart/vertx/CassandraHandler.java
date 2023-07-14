package com.smart.vertx;

import io.vertx.rxjava3.cassandra.CassandraClient;
import io.vertx.rxjava3.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.annotation
 * @date 2022/6/24 10:44
 */
@Slf4j
public class CassandraHandler {

    @Bean
    @ConditionalOnMissingBean
    public CassandraClient cassandraClient(Vertx vertx, CassandraProperties cassandras) {
        cassandras.getServers().forEach(cassandras::addContactPoint);
        if (cassandras.isShared()) {
            return CassandraClient.create(vertx, cassandras);
        } else {
            return CassandraClient.createShared(vertx, cassandras.getSharedClientName(), cassandras);
        }
    }

}
