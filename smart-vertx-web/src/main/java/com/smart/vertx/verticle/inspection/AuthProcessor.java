package com.smart.vertx.verticle.inspection;

import com.smart.vertx.annotation.AuthIgnore;
import com.smart.vertx.auth.AuthProperties;
import com.smart.vertx.auth.UserService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Objects;

import static com.smart.vertx.constant.CommonConstant.*;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle.inspection
 * @date 2022/8/1 15:49
 */
@Service
@Slf4j
public class AuthProcessor implements IProcessorChain {
    @Resource
    AuthProperties authProperties;
    @Resource
    UserService userService;

    @Override
    public int order() {
        return 0;
    }

    @Override
    public Single<Boolean> handler(CheckParams request) {
        Method method = request.getMethod();
        RoutingContext s = request.getS();
        //auth check
        if (authProperties.isEnable()) {
            AuthIgnore authIgnore = method.getAnnotation(AuthIgnore.class);
            if (Objects.isNull(authIgnore)) {
                String authorization = s.request().getHeader(X_TOKEN);
                if (StringUtils.isBlank(authorization)) {
                    s.fail(HttpResponseStatus.FORBIDDEN.code());
                    log.debug("system auth open,check fail.{}", 403);
                    return Single.just(false);
                }
                Vertx.currentContext().put(USER_TOKEN, authorization);
                return userService.auth(authProperties.getRealm()).flatMap(u -> {
                    log.debug("system auth check success,{}", u.getPreferredUsername());
                    Vertx.currentContext().put(USER_INFO, u);
                    return nextProcessor().handler(request);
                }).doOnError(e -> {
                    log.error(" auth error,{}", e.getLocalizedMessage());
                    s.fail(HttpResponseStatus.UNAUTHORIZED.code(), e);
                });
            }
        }
        return nextProcessor().handler(request);
    }
}
