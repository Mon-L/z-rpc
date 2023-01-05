package cn.zcn.rpc.remoting.connection;

import cn.zcn.rpc.remoting.Url;
import io.netty.util.concurrent.Future;

/**
 * 相同 {@code Url} 的 {@link Connection} 集合，可以通过该接口获取连接和释放连接。<p>
 * {@code ConnectionGroup} 不再使用时必须调用 {@code close()} ，该方法会关闭所有 {@code Connection}。
 *
 * @author zicung
 */
public interface ConnectionGroup {

    /**
     * 获取连接组 Url
     *
     * @return 连接组对应的 {@code Url}
     */
    Url getUrl();

    /**
     * 获取一条可用的 {@code Connection}。该方法返回 {@code Connection} 时会检查是否存活。
     * 当 {@code Connection} 不存活时，会移除该 {@code Connection} 并重新获取。
     *
     * @return {@code Future<Connection>}
     */
    Future<Connection> acquireConnection();

    /**
     * 释放 {@code Connection}。当 {@code Connection} 不属于此连接组时会关闭 {@code Connection}。
     *
     * @param connection {@code Connection}
     * @return {@code Future<Connection>}
     */
    Future<Void> releaseConnection(Connection connection);

    /**
     * 获取当前活动的连接数。
     *
     * @return 活动连接数
     */
    int getActiveCount();

    /**
     * 是否可以关闭连接组。<p>
     * 符合以下情况时可以关闭连接组：
     * <ul>
     *     <li>当所有 {@code Connection} 都不可用时。</li>
     *     <li>所有 {@code Connection} 的请求都已完成时，即 {@code Connection} 中没有未完成的 {@code InvocationPromise}。</li>
     * </ul>
     *
     * @return {@code true}, 可以关闭连接组; {@code false} 不能关闭连接组。
     */
    boolean canClose();

    /**
     * 连接组最后一次被请求的时间
     *
     * @return 最后一次被请求的时间，单位毫秒
     */
    long getLastAcquiredTime();

    /**
     * 关闭连接组
     *
     * @return close future.
     */
    Future<Void> close();
}
