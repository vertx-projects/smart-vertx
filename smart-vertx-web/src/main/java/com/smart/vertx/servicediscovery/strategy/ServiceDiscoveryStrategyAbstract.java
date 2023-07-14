package com.smart.vertx.servicediscovery.strategy;

import com.smart.vertx.annotation.BasicAuthentication;
import com.smart.vertx.annotation.RequestMapping;
import com.smart.vertx.annotation.Sign;
import com.smart.vertx.annotation.WebClient;
import com.smart.vertx.core.sign.SignHandler;
import com.smart.vertx.enums.CommonConstEnums;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import io.vertx.rxjava3.servicediscovery.ServiceDiscovery;
import io.vertx.rxjava3.servicediscovery.types.HttpEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Objects;

import static com.smart.vertx.constant.CommonConstant.USER_TOKEN;
import static com.smart.vertx.verticle.SuperVerticle.springContext;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.servicediscovery.strategy
 * @date 2022/6/16 16:55
 */
@Slf4j
@Service
public class ServiceDiscoveryStrategyAbstract extends RequestStrategyAbstract {
    @Resource
    ServiceDiscovery serviceDiscovery;

    @Override
    public Single<Object> invoke(Method method, Object[] args, WebClient webClient, RequestMapping mapping) {
        String path = webClient.path().concat(mapping.value());
        Single<HttpRequest<Buffer>> request = HttpEndpoint
                .getWebClient(serviceDiscovery, new JsonObject().put(CommonConstEnums.name.name(), webClient.value()))
                .map(s -> s.request(HttpMethod.valueOf(mapping.method().name()), path)
                        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), mapping.contentType())
                        .putHeader(HttpHeaderNames.ACCEPT.toString(), mapping.accept())
                        .timeout(webClientProperties.getTimeout()))
                .map(h -> {
                    Context context = Vertx.currentContext();
                    if (Objects.nonNull(context)) {
                        String token = context.get(USER_TOKEN);
                        if (StringUtils.isNotBlank(token)) {
                            h.bearerTokenAuthentication(token);
                        }
                    }
                    //基础认证添加
                    BasicAuthentication basic = method.getAnnotation(BasicAuthentication.class);
                    if (Objects.nonNull(basic)) return h.basicAuthentication(basic.id(), basic.password());
                    return h;
                });
        //复杂验签认证添加
        Sign sign = method.getAnnotation(Sign.class);
        if (Objects.nonNull(sign)) {
            springContext.getBean(SignHandler.class).invoke(method.getParameters(), args);
        }
        return getSendResult(webClient.fallback(), method, args, request);
    }

    @Override
    public boolean none() {
        return true;
    }
}
