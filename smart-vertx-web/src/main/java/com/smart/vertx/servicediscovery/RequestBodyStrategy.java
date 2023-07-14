package com.smart.vertx.servicediscovery;

import com.smart.vertx.annotation.RequestBody;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import org.springframework.stereotype.Service;

import java.lang.reflect.Parameter;

/**
 * @author peng.bo
 * @date 2022/5/24 11:54
 * @desc
 */
@Service("requestBody")
public class RequestBodyStrategy implements IParamStrategy {
    @Override
    public Object init(Object data, Parameter parameter, Single<HttpRequest<Buffer>> request) {
        return data;
    }


    @Override
    public String getParameter() {
        return RequestBody.class.getName();
    }


}
