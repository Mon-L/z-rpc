package cn.zcn.rpc.remoting.connection;

import cn.zcn.rpc.remoting.Url;
import cn.zcn.rpc.remoting.utils.NetUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

/**
 * 管理仅包含一条 {@link Connection} 的连接组，多个 {@code Thread} 可同时使用该 {@code Connection}。
 *
 * @author zicung
 */
public class SingleConnectionGroup extends AbstractConnectionGroup {

    private volatile Promise<Connection> connectionPromise;
    private volatile Promise<Void> closePromise;

    public SingleConnectionGroup(Url url, Bootstrap bootstrap) {
        super(url, bootstrap);
    }

    @Override
    public long getLastAcquiredTime() {
        return super.getLastAcquiredTime();
    }

    private Connection getConnectionIfPresent() {
        if (connectionPromise != null && connectionPromise.isSuccess()) {
            return connectionPromise.getNow();
        }
        return null;
    }

    @Override
    public int getActiveCount() {
        Connection conn = getConnectionIfPresent();
        return conn != null && conn.isActive() ? 1 : 0;
    }

    /**
     * 获取 {@code Connection}。<p>
     * <ul>
     *     <li>当不存在 {@code Connection} 时，新建一条连接并返回。</li>
     *     <li>当存在一条可用 {@code Connection} 时直接返回。</li>
     *     <li>当获得一条 {@code Connection} 时会检查其是否存活，当 {@code Connection} 不存活是会移除它并重新获取。</li>
     * </ul>
     *
     * @param promise acquire promise
     */
    @Override
    protected void doAcquireConnection(Promise<Connection> promise) {
        Connection connection = getConnectionIfPresent();
        if (connection != null) {
            if (connection.isActive()) {
                //有可用的connection
                promise.setSuccess(connection);
                return;
            } else {
                connection.close();
            }
        }

        if (connectionPromise != null && !connectionPromise.isDone()) {
            //正在建立连接
            connectionPromise.addListener(new CreateConnectionListener(promise));
            return;
        }

        //建立一条新连接
        connectionPromise = executor.newPromise();
        connectionPromise.addListener(new CreateConnectionListener(promise));
        createConnection(connectionPromise);
    }

    @Override
    protected void doReleaseConnection(Promise<Void> promise, Connection connection) {
        Url groupKey = connection.getChannel().attr(Connection.CONNECTION_GROUP_KEY).get();
        if (url.equals(groupKey)) {
            if (!connection.isActive()
                    || !connectionPromise.isDone()
                    || !connectionPromise.isSuccess()
                    || connectionPromise.getNow() != connection) {
                connection.close();
            }
            promise.setSuccess(null);
        } else {
            connection.close();
            promise.setFailure(new IllegalArgumentException("Connection " + NetUtil.getRemoteAddress(connection.getChannel()) +
                    " was not acquired from this ConnectionGroup"));
        }
    }

    @Override
    public boolean canClose() {
        if (connectionPromise != null) {
            if (!connectionPromise.isDone()) {
                //正在连接,不能关闭
                return false;
            } else if (connectionPromise.isSuccess()) {
                Connection connection = connectionPromise.getNow();

                //存在可用的连接且还有未完成的请求,不能关闭
                return connection.getInvokeFutures().isEmpty() || !connection.isActive();
            }
        }
        return true;
    }

    @Override
    public Future<Void> close() {
        if (isClosed.compareAndSet(false, true)) {
            closePromise = executor.newPromise();

            Connection conn = getConnectionIfPresent();
            if (conn == null) {
                closePromise.setSuccess(null);
            } else {
                conn.close().addListener(c -> {
                    if (c.isSuccess()) {
                        closePromise.setSuccess(null);
                    } else {
                        closePromise.setFailure(c.cause());
                    }
                });
            }
        }

        return closePromise;
    }

    private class CreateConnectionListener implements GenericFutureListener<Future<Connection>> {

        private final Promise<Connection> promise;

        private CreateConnectionListener(Promise<Connection> promise) {
            this.promise = promise;
        }

        @Override
        public void operationComplete(Future<Connection> future) {
            if (isClosed.get()) {
                if (future.isSuccess()) {
                    future.getNow().close();
                }

                reportGroupClosed(promise);
            } else {
                if (future.isSuccess()) {
                    promise.setSuccess(future.getNow());
                } else {
                    promise.setFailure(future.cause());
                }
            }
        }
    }
}
