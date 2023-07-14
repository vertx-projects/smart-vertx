package com.smart.vertx.verticle;

import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.server.GrpcServer;
import lombok.Data;

/**
 * @author peng.bo
 * @date 2022/5/25 16:39
 * @desc
 */
@Data
public class GrpcProxy {
    public GrpcProxy() {
    }

    private GrpcServer grpcServer;
    private SocketAddress socketAddress;
    private GrpcClient grpcClient;

    public GrpcProxy(SocketAddress socketAddress, GrpcClient grpcClient) {
        this.socketAddress = socketAddress;
        this.grpcClient = grpcClient;
    }
}
