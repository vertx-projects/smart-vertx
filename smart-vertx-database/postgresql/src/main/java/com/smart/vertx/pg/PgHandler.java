package com.smart.vertx.pg;

import com.alibaba.druid.pool.DruidDataSource;
import com.smart.vertx.auth.User;
import com.smart.vertx.constant.CommonConstant;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.pg
 * @date 2022/8/19 12:50
 */
@Slf4j
public class PgHandler {

    @Resource
    private Environment environment;
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @Bean
    public Object pgServer(PgProperties properties) {
        if (Objects.nonNull(properties.getDataSource())) {
            properties.getDataSource().forEach((k, v) -> {
                database(k, v, INITIALIZED.compareAndSet(false, true));
                log.info("init DB [{}] success.", k);
            });
            return "OK";
        }
        return database(properties.getName(), properties, true);
    }

    @Bean
    public Object flyway(PgProperties properties) {
        String[] profiles = environment.getActiveProfiles();
        boolean profile = ArrayUtils.isNotEmpty(profiles) && profiles[0].equals("prod");
        if (Objects.nonNull(properties.getDataSource())) {
            properties.getDataSource().forEach((k, v) -> {
                flyway(profile, v, String.format("db/migration/%s", k));
            });
            return "OK";
        }
        return flyway(profile, properties, "db/migration");
    }

    @Nullable
    private Database database(String name, DataSource dataSource, boolean defaultServer) {
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setDataSource(dataSource);
        databaseConfig.setDefaultServer(defaultServer);
        databaseConfig.setName(name);
        databaseConfig.setCurrentUserProvider(() -> {
            Object o = Vertx.currentContext().get(CommonConstant.USER_INFO);
            if (Objects.nonNull(o)) {
                User user = (User) o;
                return user.getUsername();
            }
            return null;
        });
        return DatabaseFactory.create(databaseConfig);
    }

    private MigrateResult flyway(boolean profile, DruidDataSource dataSource, String location) {
        Flyway flyway = Flyway.configure()
                .outOfOrder(!profile)
                .encoding("UTF-8")
                .cleanDisabled(true)
                .validateOnMigrate(true)
                .table("flyway_schema_history")
                .dataSource(dataSource)
                .locations(location)
                .baselineOnMigrate(true)
                .load();
        return flyway.migrate();
    }
}
