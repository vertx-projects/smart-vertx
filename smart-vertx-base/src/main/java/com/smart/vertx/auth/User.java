package com.smart.vertx.auth;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.verticle.auth
 * @date 2022/6/24 12:39
 */
@Data
public class User {
    @JSONField(name = "sub")
    private String userId;
    @JSONField(name = "email_verified")
    private Boolean emailVerified;
    @JSONField(name = "name")
    private String username;
    @JSONField(name = "preferred_username")
    private String preferredUsername;
    @JSONField(name = "given_name")
    private String givenName;
    @JSONField(name = "family_name")
    private String familyName;
    private String email;
}
