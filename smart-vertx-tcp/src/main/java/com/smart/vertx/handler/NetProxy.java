package com.smart.vertx.handler;

import io.vertx.rxjava3.core.net.NetSocket;
import lombok.Data;

/**
 * @author peng.bo
 * @date 2022/5/25 16:39
 * @desc
 */
@Data
public class NetProxy {
    private NetSocket netSocket;
}
