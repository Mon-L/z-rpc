package cn.zcn.rpc.remoting.connection;

import cn.zcn.rpc.remoting.InvokePromise;
import cn.zcn.rpc.remoting.Url;
import cn.zcn.rpc.remoting.config.Option;
import cn.zcn.rpc.remoting.config.RpcOptions;
import cn.zcn.rpc.remoting.exception.TransportException;
import cn.zcn.rpc.remoting.protocol.ResponseCommand;
import cn.zcn.rpc.remoting.utils.NetUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Connection {

    public static final AttributeKey<Connection> CONNECTION_KEY = AttributeKey.valueOf("rpc-connection");
    public static final AttributeKey<Url> CONNECTION_GROUP_KEY = AttributeKey.valueOf("connection-group-key");

    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

    private final Channel channel;

    /**
     * 心跳失败次数
     */
    private int heartbeatFailures = 0;

    private final ConcurrentMap<Integer, InvokePromise<ResponseCommand>> promises = new ConcurrentHashMap<>();

    public Connection(Channel channel) {
        this.channel = channel;
        init();
    }

    private void init() {
        channel.closeFuture().addListener((GenericFutureListener<Future<Void>>) future -> {
            // if connection closed, notify uncompleted promise.
            Iterator<Map.Entry<Integer, InvokePromise<ResponseCommand>>> iterator = promises.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Integer, InvokePromise<ResponseCommand>> entry = iterator.next();
                iterator.remove();

                InvokePromise<ResponseCommand> promise = entry.getValue();
                promise.cancelTimeout();
                promise.setFailure(new TransportException("Connection was closed. Request id:{0}, Remoting address:{1}",
                        entry.getKey(), NetUtil.getRemoteAddress(channel)));
            }
        });
    }

    public int getHeartbeatFailures() {
        return heartbeatFailures;
    }

    public void setHeartbeatFailures(int failures) {
        heartbeatFailures = failures;
    }

    public InvokePromise<ResponseCommand> removePromise(Integer id) {
        return promises.remove(id);
    }

    public void addPromise(int id, InvokePromise<ResponseCommand> promise) {
        promises.put(id, promise);
    }

    public boolean isActive() {
        return channel.isActive();
    }

    public Channel getChannel() {
        return channel;
    }

    public <T> T getOption(Option<T> option) {
        return channel.attr(RpcOptions.OPTIONS_ATTRIBUTE_KEY).get().getOption(option);
    }

    public Map<Integer, InvokePromise<ResponseCommand>> getInvokeFutures() {
        return promises;
    }

    public ChannelFuture close() {
        if (channel.isActive()) {
            return channel.close().addListener((ChannelFutureListener) f -> {
                if (!f.isSuccess()) {
                    LOGGER.warn("Failed to close connection. Url:{}", NetUtil.getRemoteAddress(channel), f.cause());
                } else {
                    LOGGER.debug("Connection was closed. Url:{}", NetUtil.getRemoteAddress(channel));
                }
            });
        } else {
            return channel.newSucceededFuture();
        }
    }
}
