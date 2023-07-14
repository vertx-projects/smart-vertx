package com.smart.vertx.servicediscovery.strategy;

import com.smart.vertx.annotation.BasicAuthentication;
import com.smart.vertx.annotation.RequestMapping;
import com.smart.vertx.annotation.Sign;
import com.smart.vertx.annotation.WebClient;
import com.smart.vertx.core.sign.SignHandler;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import io.vertx.rxjava3.uritemplate.UriTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Objects;

import static com.smart.vertx.constant.CommonConstant.USER_TOKEN;
import static com.smart.vertx.verticle.SuperVerticle.springContext;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.servicediscovery.strategy
 * @date 2022/6/16 16:54
 */
@Service
public class WebClientStrategyAbstract extends RequestStrategyAbstract {
    final io.vertx.rxjava3.ext.web.client.WebClient webClient;
    @Resource
    private Environment environment;

    public WebClientStrategyAbstract(io.vertx.rxjava3.ext.web.client.WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Single<Object> invoke(Method method, Object[] args, WebClient client, RequestMapping mapping) {
        String url = this.environment.resolvePlaceholders(client.url());
        boolean ssl = url.startsWith("https");
        URI uri = URI.create(url.startsWith("http") ? url : "http://".concat(url));
        String path = client.path().concat(mapping.value());
        if (StringUtils.isNotBlank(uri.getPath())) {
            path = uri.getPath().concat(path);
        }
        UriTemplate REQUEST_URI = UriTemplate.of(path);
        Single<HttpRequest<Buffer>> request = Single.defer(() -> {
            HttpRequest<Buffer> h = webClient.request(HttpMethod.valueOf(mapping.method().name()), uri.getPort(), uri.getHost(), REQUEST_URI).putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), mapping.contentType()).putHeader(HttpHeaderNames.ACCEPT.toString(), mapping.accept()).ssl(ssl).timeout(webClientProperties.getTimeout());
            return Single.just(h);
        }).map(h -> {
            Context context = Vertx.currentContext();
            if (Objects.nonNull(context)) {
                String token = context.get(USER_TOKEN);
                if (StringUtils.isNotBlank(token)) {
                    h.bearerTokenAuthentication(token);
                }
            }
            return h;
        }).map(h -> {
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
        return getSendResult(client.fallback(), method, args, request);
    }

    @Override
    public boolean none() {
        return false;
    }
}
