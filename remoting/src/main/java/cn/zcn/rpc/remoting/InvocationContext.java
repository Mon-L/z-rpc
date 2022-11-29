package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.RpcStatus;

public interface InvocationContext {

    /**
     * 请求方IP
     */
    String getRemoteHost();

    /**
     * 请求方端口
     */
    int getRemotePort();

    /**
     * 请求ID
     */
    int getRequestId();

    /**
     * 请求进入等待队列的时间
     */
    long getReadyTimeMillis();

    /**
     * 请求开始执行的时间
     */
    long getStartTimeMillis();

    /**
     * 请求处理是否超时
     */
    boolean isTimeout();

    /**
     * 发送成功响应
     */
    void writeAndFlushSuccessfullyResponse(Object obj);

    /**
     * 发送错误响应
     */
    void writeAndFlushResponse(Object obj, RpcStatus status);
}
