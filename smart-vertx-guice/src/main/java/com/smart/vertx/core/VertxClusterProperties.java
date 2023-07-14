package com.smart.vertx.core;

import com.google.inject.Singleton;
import io.vertx.core.net.JksOptions;
import lombok.Builder;
import lombok.Data;


/**
 * @author peng.bo
 * @date 2022/5/19 13:28
 * @desc
 */
@Data
@Singleton
public class VertxClusterProperties {
    private boolean ssl;
    private JksOptions keyStore;
    private JksOptions trustStore;
}
