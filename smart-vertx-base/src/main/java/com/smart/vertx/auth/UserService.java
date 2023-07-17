package com.smart.vertx.auth;

import com.smart.vertx.annotation.PathVariable;
import com.smart.vertx.annotation.RequestMapping;
import com.smart.vertx.enums.RequestMethod;
import com.smart.vertx.annotation.WebClient;
import io.reactivex.rxjava3.core.Single;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle
 * @date 2022/6/24 12:34
 */
@WebClient(url = "${vertx.auth.host:https://sso.smart.com:443}", path = "/auth/realms")
public interface UserService {
    @RequestMapping(method = RequestMethod.POST,
            value = "/{realm}/protocol/openid-connect/userinfo")
    Single<User> auth(@PathVariable("realm") String realm);
}
