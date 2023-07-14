package com.smart.vertx.servicediscovery.strategy;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Maps;
import com.smart.vertx.annotation.RequestMapping;
import com.smart.vertx.annotation.WebClient;
import com.smart.vertx.client.WebClientProperties;
import com.smart.vertx.exception.VertxResCriteriaException;
import com.smart.vertx.servicediscovery.IParamStrategy;
import com.smart.vertx.servicediscovery.WebClientProxyFactory;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.vertx.core.Future;
import io.vertx.rxjava3.circuitbreaker.CircuitBreaker;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import io.vertx.rxjava3.ext.web.client.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author pengbo
 * @version V1.1.11
 * @Package com.smart.vertx.servicediscovery.strategy
 * @date 2022/6/16 16:56
 * @update 2022/7/25 16:56
 */
@Slf4j
public abstract class RequestStrategyAbstract {
    @Resource
    CircuitBreaker circuitBreaker;
    @Resource
    WebClientProperties webClientProperties;

    public abstract Single<Object> invoke(Method method, Object[] args, WebClient webClient, RequestMapping mapping);

    private static final Map<Boolean, RequestStrategyAbstract> strategyMap = Maps.newConcurrentMap();

    public static RequestStrategyAbstract get(boolean blank) {
        return strategyMap.get(blank);
    }

    protected abstract boolean none();

    @PostConstruct
    public void initStrategy() {
        strategyMap.put(none(), this);
    }

    protected Single<Object> getSendResult(Class<?> fallback, Method method, Object[] args, Single<HttpRequest<Buffer>> request) {
        Object data = null;
        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];
            Object result = IParamStrategy.get(parameter).init(args[i], parameter, request);
            if (result instanceof SingleSource<?>) {
                request = (Single<HttpRequest<Buffer>>) result;
            } else {
                data = result;
            }
        }
        final Single<HttpRequest<Buffer>> finalRequest = request;
        final Object finalData = data;
        return Single.just(void.class.equals(fallback)).flatMap(r -> {
            if (r) {
                return finalRequest.flatMap(s -> sendValid(method, finalRequest.flatMap(y -> Objects.isNull(finalData) ? y.rxSend() : y.rxSendJson(finalData))));
            }
            return circuitBreaker.executeWithFallback(promise -> Future.fromCompletionStage(sendValid(method, finalRequest.flatMap(s -> Objects.isNull(finalData) ? s.rxSend() : s.rxSendJson(finalData)))
                    .toCompletionStage()).onComplete(promise.getDelegate()), v -> {
                log.debug("trigger circuit fallback breaker,", v);
                try {
                    Optional<Method> optional = Arrays.stream(fallback.getDeclaredMethods()).filter(s -> s.getName().equals(method.getName())).findFirst();
                    if (optional.isPresent()) {
                        Method fallbackMethod = optional.get();
                        return fallbackMethod.invoke(WebClientProxyFactory.fallbackMap.get(fallback.getInterfaces()[0].getName()), args);
                    }
                    throw new VertxResCriteriaException("trigger circuit default fallback,please set your fallback method.");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    protected Single<Object> sendValid(Method method, Single<HttpResponse<Buffer>> response) {
        return response.map(t -> {
            log.debug("response body:{}", t.bodyAsString());
            if (t.statusCode() == 200) {
                if (JSON.isValid(t.bodyAsString())) {
                    Type type = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                    if (type instanceof ParameterizedType) {
                        return JSON.parseObject(t.bodyAsString(), ((ParameterizedType) type).getRawType());
                    }
                    return JSON.parseObject(t.bodyAsString(), type);
                } else {
                    return t.bodyAsString();
                }
            }
            throw new VertxResCriteriaException("response error," + t.bodyAsString());
        });
    }
}
