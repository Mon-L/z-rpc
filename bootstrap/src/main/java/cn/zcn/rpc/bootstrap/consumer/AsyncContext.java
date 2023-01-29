package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.RpcResponse;

import java.util.concurrent.Future;

/**
 * @author zicung
 */
public class AsyncContext {

    private static final ThreadLocal<Future<RpcResponse>> FUTURES = ThreadLocal.withInitial(() -> null);

    public static Future<RpcResponse> getFuture() {
        return FUTURES.get();
    }

    public static void setFuture(Future<RpcResponse> future) {
        FUTURES.set(future);
    }
}
