package com.smart.vertx.servicediscovery;

import com.smart.vertx.annotation.PathVariable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Parameter;

/**
 * @author peng.bo
 * @date 2022/5/24 13:43
 * @desc
 */
@Service("pathVariable")
public class PathVariableStrategy implements IParamStrategy {
    @Override
    public Object init(Object data, Parameter parameter, Single<HttpRequest<Buffer>> request) {
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        String param = StringUtils.isBlank(pathVariable.value()) ? parameter.getName() : pathVariable.value();
        return request.map(s -> s.setTemplateParam(param, data.toString()));
    }

    @Override
    public String getParameter() {
        return PathVariable.class.getName();
    }
}
