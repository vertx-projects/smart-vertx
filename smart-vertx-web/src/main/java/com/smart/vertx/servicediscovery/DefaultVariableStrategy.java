package com.smart.vertx.servicediscovery;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import org.springframework.stereotype.Service;

import java.lang.reflect.Parameter;

/**
 * @author peng.bo
 * @date 2022/5/24 13:43
 * @desc
 */
@Service("defaultVariable")
public class DefaultVariableStrategy implements IParamStrategy {
    @Override
    public Object init(Object data, Parameter parameter, Single<HttpRequest<Buffer>> request) {
        return request.map(s -> s.addQueryParam(parameter.getName(), data.toString()));
    }

    @Override
    public String getParameter() {
        return Object.class.getName();
    }

}
