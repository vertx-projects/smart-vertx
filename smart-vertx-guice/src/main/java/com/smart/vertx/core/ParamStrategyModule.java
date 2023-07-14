package com.smart.vertx.core;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.smart.vertx.verticle.*;

/**
 * @author peng.bo
 * @date 2022/6/6 18:45
 * @desc
 */
public class ParamStrategyModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<IParamStrategy> multiBinder = Multibinder.newSetBinder(binder(), IParamStrategy.class);
        multiBinder.addBinding().to(DefaultVariableStrategy.class);
        multiBinder.addBinding().to(PathVariableStrategy.class);
        multiBinder.addBinding().to(RequestParamStrategy.class);
        multiBinder.addBinding().to(RequestBodyStrategy.class);
        multiBinder.addBinding().to(RoutingContextStrategy.class);
    }
}
