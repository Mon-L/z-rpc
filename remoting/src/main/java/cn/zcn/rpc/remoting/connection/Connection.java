package cn.zcn.rpc.remoting.connection;

import cn.zcn.rpc.remoting.InvocationPromise;
import cn.zcn.rpc.remoting.config.Option;
import cn.zcn.rpc.remoting.constants.AttributeKeys;
import cn.zcn.rpc.remoting.exception.TransportException;
import cn.zcn.rpc.remoting.protocol.ResponseCommand;
import cn.zcn.rpc.remoting.utils.NetUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author zicung */
public class Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

    private final Channel channel;

    /** 心跳失败次数 */
    private int heartbeatFailures = 0;

    private final ConcurrentMap<Integer, InvocationPromise<ResponseCommand>> promises = new ConcurrentHashMap<>();

    public Connection(Channel channel) {
        this.channel = channel;
        init();
    }

    private void init() {
        channel.closeFuture().addListener((GenericFutureListener<Future<Void>>) future -> {
            // if connection closed, notify uncompleted promise.
            Iterator<Map.Entry<Integer, InvocationPromise<ResponseCommand>>> iterator = promises.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Integer, InvocationPromise<ResponseCommand>> entry = iterator.next();
                iterator.remove();

                InvocationPromise<ResponseCommand> promise = entry.getValue();
                promise.cancelTimeout();
                promise.setFailure(new TransportException(
                    "Connection was closed. Request id:{}, Remoting address:{}",
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

    public InvocationPromise<ResponseCommand> removePromise(Integer id) {
        return promises.remove(id);
    }

    public void addPromise(int id, InvocationPromise<ResponseCommand> promise) {
        promises.put(id, promise);
    }

    public boolean isActive() {
        return channel.isActive();
    }

    public Channel getChannel() {
        return channel;
    }

    public <T> T getOption(Option<T> option) {
        return channel.attr(AttributeKeys.OPTIONS).get().getOption(option);
    }

    public Map<Integer, InvocationPromise<ResponseCommand>> getInvokeFutures() {
        return promises;
    }

    public ChannelFuture close() {
        if (channel.isActive()) {
            return channel.close().addListener((ChannelFutureListener) f -> {
                if (!f.isSuccess()) {
                    LOGGER.warn("Failed to close connection. Url:{}", NetUtil.getRemoteAddress(channel), f.cause());
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Connection was closed. Url:{}", NetUtil.getRemoteAddress(channel));
                    }
                }
            });
        } else {
            return channel.newSucceededFuture();
        }
    }
}
