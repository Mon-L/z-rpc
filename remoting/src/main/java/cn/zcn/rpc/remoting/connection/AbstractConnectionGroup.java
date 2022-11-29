package cn.zcn.rpc.remoting.connection;

import cn.zcn.rpc.remoting.Url;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractConnectionGroup implements ConnectionGroup {

    private volatile long lastAcquiredTimeMillis;

    protected final Url url;
    protected final Bootstrap bootstrap;
    protected final EventExecutor executor;
    protected final AtomicBoolean isClosed = new AtomicBoolean(false);

    public AbstractConnectionGroup(Url url, Bootstrap bootstrap) {
        this.url = url;
        this.bootstrap = bootstrap;
        this.executor = bootstrap.config().group().next();
    }

    @Override
    public long getLastAcquiredTime() {
        return lastAcquiredTimeMillis;
    }

    @Override
    public Future<Connection> acquireConnection() {
        lastAcquiredTimeMillis = System.currentTimeMillis();
        Promise<Connection> newPromise = executor.newPromise();

        if (isClosed.get()) {
            //当连接组已关闭时返回异常信息
            reportGroupClosed(newPromise);
        } else if (executor.inEventLoop()) {
            doAcquireConnection(newPromise);
        } else {
            executor.execute(() -> {
                if (isClosed.get()) {   //当连接组已关闭时返回异常信息
                    reportGroupClosed(newPromise);
                } else {
                    doAcquireConnection(newPromise);
                }
            });
        }

        return newPromise;
    }

    @Override
    public Future<Void> releaseConnection(Connection connection) {
        Promise<Void> newPromise = executor.newPromise();

        if (isClosed.get()) {
            //当连接组已关闭时，关闭连接并返回异常信息
            connection.close();
            reportGroupClosed(newPromise);
        } else if (executor.inEventLoop()) {
            doReleaseConnection(newPromise, connection);
        } else {
            executor.execute(() -> {
                if (isClosed.get()) {   //当连接组已关闭时，关闭连接并返回异常信息
                    connection.close();
                    reportGroupClosed(newPromise);
                } else {
                    doReleaseConnection(newPromise, connection);
                }
            });
        }

        return newPromise;
    }

    protected void reportGroupClosed(Promise<?> promise) {
        promise.setFailure(new IllegalStateException("Connection group was closed. Url:" + url.toString()));
    }

    protected void createConnection(final Promise<Connection> promise) {
        ChannelFuture future = bootstrap.connect(url.getAddress());
        if (future.isDone()) {
            afterConnectionCreated(future, promise);
        } else {
            future.addListener((ChannelFutureListener) f -> afterConnectionCreated(f, promise));
        }
    }

    private void afterConnectionCreated(ChannelFuture channelFuture, Promise<Connection> promise) {
        if (channelFuture.isSuccess()) {
            Channel channel = channelFuture.channel();
            channel.attr(Connection.CONNECTION_GROUP_KEY).set(url);

            Connection connection = new Connection(channel);
            channel.attr(Connection.CONNECTION_KEY).set(connection);
            if (!promise.trySuccess(connection)) {
                connection.close();
            }
        } else {
            promise.setFailure(channelFuture.cause());
        }
    }

    @Override
    public Url getURL() {
        return url;
    }

    protected abstract void doAcquireConnection(Promise<Connection> newPromise);

    protected abstract void doReleaseConnection(Promise<Void> promise, Connection connection);
}
