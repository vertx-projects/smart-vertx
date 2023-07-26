package com.smart.vertx;

import com.smart.vertx.enums.BuilderTypeEnum;
import graphql.schema.idl.TypeRuntimeWiring;
import org.dataloader.BatchLoaderWithContext;

import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx
 * @date 2023/7/21 16:34
 */
public interface VertxUnaryOperator {
    /**
     * 开启ws订阅
     *
     * @return 是否开启订阅
     */
    default boolean subscription() {
        return false;
    }

    /**
     * 位于resource下面的graphqls文件的路径
     *
     * @return graphqls文件的路径
     */
    default String graphSDLPath() {
        return "default.graphqls";
    }

    Map<String, UnaryOperator<TypeRuntimeWiring.Builder>> operator();

    default Map<String, BatchLoaderWithContext<String, Object>> dataLoader() {
        return Map.of();
    }
}
