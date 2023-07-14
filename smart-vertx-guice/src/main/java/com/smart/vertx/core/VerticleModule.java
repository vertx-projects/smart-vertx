package com.smart.vertx.core;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author peng.bo
 * @date 2022/6/6 18:45
 * @desc
 */
public class VerticleModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<IVerticleType> multiBinder = Multibinder.newSetBinder(binder(), IVerticleType.class);
        multiBinder.addBinding().to(ClusterVerticle.class);
        multiBinder.addBinding().to(StandaloneVerticle.class);
    }
}
