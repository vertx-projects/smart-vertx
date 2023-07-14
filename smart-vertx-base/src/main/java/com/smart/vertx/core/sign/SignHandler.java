package com.smart.vertx.core.sign;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Maps;
import com.smart.vertx.annotation.RequestBody;
import com.smart.vertx.annotation.RequestParam;
import com.smart.vertx.exception.VertxSignException;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.sign
 * @date 2022/7/5 18:51
 */
@Slf4j
@Component
public class SignHandler {
    @Getter
    private final String clientKey;
    @Getter
    private final long timestamp;
    @Resource
    ApplicationContext applicationContext;
    private final SignProperties signProperties;
    private final Map<String, Object> maps = Maps.newTreeMap();
    private final String algorithm = "HmacSHA256", digest = "digest";

    public SignHandler(SignProperties signProperties) {
        this.signProperties = signProperties;
        this.clientKey = signProperties.getClientKey();
        this.timestamp = System.currentTimeMillis() / 1000;
    }

    public void invoke(Parameter[] parameters, Object[] args) {
        buildArgs(parameters, args);
        maps.putAll(JSON.parseObject(JSON.toJSONString(this)));
        String paramValuesStr = StringUtils.join(maps.values(), "");
        Completable.defer(() -> {
            Mac mac = Mac.getInstance(algorithm);
            byte[] secretByte = signProperties.getClientSecret().getBytes(StandardCharsets.UTF_8);
            byte[] dataBytes = paramValuesStr.getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = new SecretKeySpec(secretByte, algorithm.toUpperCase(Locale.ROOT));
            mac.init(secretKey);
            byte[] doFinal = mac.doFinal(dataBytes);
            byte[] hexB = new Hex().encode(doFinal);
            for (Object arg : args) {
                if (arg instanceof Map) {
                    ((Map) arg).put(digest, new String(hexB, StandardCharsets.UTF_8));
                    ((Map) arg).putAll(maps);
                }
            }
            return Completable.complete();
        }).onErrorComplete(throwable -> {
            log.error("encode sign error,", throwable);
            return true;
        }).subscribe();
    }

    public Single<Boolean> invokeSelf(Parameter[] parameters, Object[] args) {
        if (Objects.isNull(maps.get("timestamp")) | Objects.isNull(maps.get(digest))) {
            return Single.error(new VertxSignException("required sign param is empty."));
        }
        buildArgs(parameters, args);
        //获取传入时间戳
        long timestamp = (long) maps.get("timestamp");
        int length = Long.toString(timestamp).length();
        if (length < 13) {
            timestamp = timestamp * 1000;
        }
        int c = new Date(System.currentTimeMillis()).compareTo(DateUtils.addSeconds(new Date(timestamp), 60));
        if (c > 0) {
            return Single.error(new VertxSignException("timestamp is overdue."));
        }
        //获取用户传入digest
        String validDigest = (String) maps.get(digest);
        maps.remove(digest);
        String clientSecret;
        try {
            ClientSecretHandler clientSecretHandler = applicationContext.getBean(ClientSecretHandler.class);
            clientSecret = clientSecretHandler.getClientSecret((String) maps.get("clientKey"));
        } catch (Exception e) {
            clientSecret = ClientSecretHandler.clientSecret;
        }
        String paramValuesStr = StringUtils.join(maps.values(), "");
        final String finalClientSecret = clientSecret;
        return Single.defer(() -> {
            Mac mac = Mac.getInstance(algorithm);
            byte[] secretByte = finalClientSecret.getBytes(StandardCharsets.UTF_8);
            byte[] dataBytes = paramValuesStr.getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = new SecretKeySpec(secretByte, algorithm.toUpperCase(Locale.ROOT));
            mac.init(secretKey);
            byte[] doFinal = mac.doFinal(dataBytes);
            byte[] hexB = new Hex().encode(doFinal);
            //根据用户传参生成新的digest
            String newDigest = new String(hexB, StandardCharsets.UTF_8);
            if (!newDigest.equals(validDigest)) {
                return Single.error(new VertxSignException("digest valid error."));
            }
            return Single.just(true);
        });
    }

    private void buildArgs(Parameter[] parameters, Object[] args) {
        for (int i = 0; i < parameters.length; i++) {
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (Objects.nonNull(requestParam)) {
                if (args[i] instanceof Map) {
                    maps.putAll((Map<String, ?>) args[i]);
                }
            }
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (Objects.nonNull(requestBody)) {
                if (args[i] instanceof Map) {
                    maps.putAll(JSON.parseObject(JSON.toJSONString(args[i])));
                    args[i] = maps;
                }
            }
        }
    }
}
