package com.smart.vertx.core;

import com.google.inject.*;
import com.smart.vertx.enums.VerticleTypeEnum;
import io.vertx.rxjava3.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author peng.bo
 * @date 2022/6/7 9:50
 * @desc
 */
@Slf4j
@Singleton
public class GuiceFactory {
    @Inject
    Set<IVerticleType> iVerticleTypes;

    public void runApplication(Vertx vertx, VerticleTypeEnum typeEnum) {
        iVerticleTypes.stream().filter(s -> s.name().equals(typeEnum)).findFirst().ifPresent(s -> s.start(vertx));
    }
}
