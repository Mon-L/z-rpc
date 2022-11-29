package cn.zcn.rpc.remoting.connection;

import cn.zcn.rpc.remoting.Url;
import io.netty.util.concurrent.Future;

public interface ConnectionGroup {

    /**
     * 获取连接组对应的URL
     */
    Url getURL();

    /**
     * 获取一条可用连接
     *
     * @return {@link Future<Connection>}
     */
    Future<Connection> acquireConnection();

    /**
     * 释放连接
     *
     * @param connection {@link Connection}
     * @return {@link Future<Connection>}
     */
    Future<Void> releaseConnection(Connection connection);

    /**
     * 获取可用连接数
     */
    int getActiveCount();

    /**
     * 是否可以关闭连接组
     *
     * @return 当所有连接都不可用或者所有连接的请求都已完成时返回 {@code true}，否则返回 {@code false}
     */
    boolean canClose();

    /**
     * 最后一次请求的时间
     *
     * @return millis
     */
    long getLastAcquiredTime();

    /**
     * 关闭
     *
     * @return {@link Future<Connection>}
     */
    Future<Void> close();
}
