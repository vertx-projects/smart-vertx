package com.smart.vertx.verticle.request;

import com.smart.vertx.annotation.LoginUser;
import io.vertx.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Parameter;
import java.util.List;

import static com.smart.vertx.constant.CommonConstant.USER_INFO;

/**
 * @author peng.bo
 * @date 2022/5/24 13:43
 * @desc
 */
@Service
public class LoginUserStrategy implements IParamStrategy {
    @Override
    public void init(List<Object> items, Parameter parameter, RoutingContext s) {
        LoginUser loginUser = parameter.getAnnotation(LoginUser.class);
        if (loginUser != null) {
            items.add(Vertx.currentContext().get(USER_INFO));
        }
    }

    @Override
    public String getParameter() {
        return LoginUser.class.getName();
    }


}
