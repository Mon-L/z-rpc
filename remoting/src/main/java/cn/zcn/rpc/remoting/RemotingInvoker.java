package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.config.ClientOptions;
import cn.zcn.rpc.remoting.config.Options;
import cn.zcn.rpc.remoting.connection.Connection;
import cn.zcn.rpc.remoting.connection.ConnectionGroup;
import cn.zcn.rpc.remoting.connection.ConnectionGroupManager;
import cn.zcn.rpc.remoting.exception.*;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import cn.zcn.rpc.remoting.protocol.*;
import cn.zcn.rpc.remoting.serialization.Serializer;
import cn.zcn.rpc.remoting.utils.NetUtil;
import cn.zcn.rpc.remoting.utils.TimerHolder;
import io.netty.bootstrap.Bootstrap;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 远程调用者。与远程节点建立连接并发送请求。
 *
 * @author zicung
 */
public class RemotingInvoker extends AbstractLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingInvoker.class);

    private final Options options;
    private final EventExecutor eventExecutor;
    private final Bootstrap bootstrap;
    private final Charset charset;

    private ConnectionGroupManager connectionGroupManager;

    public RemotingInvoker(Options options, Bootstrap bootstrap) {
        this.options = options;
        this.charset = Charset.forName(options.getOption(ClientOptions.CHARSET));
        this.bootstrap = bootstrap;
        this.eventExecutor = bootstrap.config().group().next();
    }

    @Override
    protected void doStart() throws LifecycleException {
        this.connectionGroupManager = new ConnectionGroupManager(bootstrap);
        this.connectionGroupManager.start();
    }

    @Override
    protected void doStop() throws LifecycleException {
        if (connectionGroupManager != null) {
            this.connectionGroupManager.stop();
        }
    }

    /**
     * 离超时还剩多少时间
     *
     * @param start 开始时间
     * @param timeout 超时时间
     * @return 剩余时间
     */
    private long getRemainingTime(long start, long timeout) {
        return start + timeout - System.currentTimeMillis();
    }

    /**
     * 创建请求
     *
     * @param payload payload
     * @param timeoutMillis 请求超时时间
     * @param commandType 请求类型
     * @return ICommand
     */
    private ICommand createRequestCommand(Object payload, int timeoutMillis, CommandType commandType) {
        Protocol protocol = ProtocolManager.getInstance().getDefaultProtocol();
        CommandFactory commandFactory = protocol.getCommandFactory();
        RequestCommand req = commandFactory.createRequestCommand(commandType, CommandCode.REQUEST);

        byte[] clazz = payload.getClass().getName().getBytes(charset);
        req.setClazz(clazz);

        byte serializerCode = SerializerManager.DEFAULT_SERIALIZER;
        Serializer serializer = SerializerManager.getSerializer(SerializerManager.DEFAULT_SERIALIZER);
        if (serializer == null) {
            throw new SerializationException("Unknown serializer with " + serializerCode);
        }

        req.setSerializer(serializerCode);
        req.setContent(serializer.serialize(payload));

        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 0);
        if (options.getOption(ClientOptions.USE_CRC32)) {
            protocolSwitch.turnOn(0);
        }
        req.setProtocolSwitch(protocolSwitch);

        req.setTimeout(timeoutMillis);
        return req;
    }

    private Object deserialize(ResponseCommand responseCommand) throws SerializationException {
        Serializer serializer = SerializerManager.getSerializer(responseCommand.getSerializer());
        if (serializer == null) {
            throw new SerializationException("Unknown serializer with " + responseCommand.getSerializer());
        }

        String clazz = new String(responseCommand.getClazz(), charset);

        return serializer.deserialize(responseCommand.getContent(), clazz);
    }

    /**
     * 发送单向请求，这种类型的请求不会有响应结果
     *
     * @param url 请求路径
     * @param obj 请求体
     * @return Future<Void>
     */
    public Future<Void> oneWayInvoke(Url url, Object obj) {
        if (!isStarted()) {
            return eventExecutor.newFailedFuture(new TransportException("RemotingInvoker is closed."));
        }

        Promise<Void> promise = eventExecutor.newPromise();

        try {
            // 创建请求
            ICommand req = createRequestCommand(obj, -1, CommandType.REQUEST_ONEWAY);

            // 获取连接组
            ConnectionGroup connectionGroup = connectionGroupManager.getConnectionGroup(url);

            connectionGroup.acquireConnection().addListener((GenericFutureListener<Future<Connection>>) connFuture -> {
                if (connFuture.isSuccess()) {
                    // 获取连接成功
                    Connection conn = connFuture.get();

                    if (checkInvalidOrNot(promise, conn)) {
                        return;
                    }

                    try {
                        conn.getChannel().writeAndFlush(req).addListener(future -> {
                            if (!future.isSuccess()) {
                                LOGGER.error(
                                    "Unexpected exception when write request. Request id:{}, To:{}",
                                    req.getId(),
                                    NetUtil.getRemoteAddress(conn.getChannel()),
                                    future.cause());
                                promise.setFailure(new TransportException(future.cause().getMessage(), future.cause()));
                            } else {
                                promise.setSuccess(null);
                            }
                        });
                    } finally {
                        connectionGroup.releaseConnection(conn);
                    }
                } else {
                    // 获取连接失败
                    promise.setFailure(new TransportException(
                        "Failed to acquire connection. " + "Request id:{}, To:{}",
                        req.getId(),
                        NetUtil.getRemoteAddress(url.getAddress()),
                        connFuture.cause()));
                }
            });
        } catch (Throwable t) {
            promise.setFailure(new TransportException(t.getMessage(), t));
        }

        return promise;
    }

    /**
     * 发送具有响应结果的请求
     *
     * @param url 请求路径
     * @param obj 请求体
     * @param timeoutMillis 响应超时时间
     */
    @SuppressWarnings({ "unchecked" })
    public <T> Future<T> invoke(Url url, Object obj, int timeoutMillis) {
        if (!isStarted()) {
            return eventExecutor.newFailedFuture(new TransportException("RemotingInvoker is closed."));
        }

        long startMillis = System.currentTimeMillis();
        Promise<T> promise = eventExecutor.newPromise();

        try {
            // 创建请求体
            ICommand req = createRequestCommand(obj, timeoutMillis, CommandType.REQUEST);

            // 获取连接组
            ConnectionGroup connectionGroup = connectionGroupManager.getConnectionGroup(url);

            InvocationPromise<ResponseCommand> invocationPromise = new DefaultInvocationPromise(
                eventExecutor.newPromise());

            // 判断请求是否已超时
            if (getRemainingTime(startMillis, timeoutMillis) <= 0) {
                invocationPromise.setFailure(new TimeoutException(
                    "Send request timeout. Request id:{}, To:{}",
                    req.getId(), NetUtil.getRemoteAddress(url.getAddress())));
                return promise;
            }

            //获取连接
            connectionGroup.acquireConnection().addListener((GenericFutureListener<Future<Connection>>) connFuture -> {
                long remainingTime = getRemainingTime(startMillis, timeoutMillis);
                if (remainingTime <= 0) {
                    // 请求已超时，返回超时异常
                    invocationPromise.setFailure(new TimeoutException(
                        "Send request timeout. Request id:{}, To:{}",
                        req.getId(), NetUtil.getRemoteAddress(url.getAddress())));

                    // 释放连接
                    if (connFuture.isSuccess()) {
                        connectionGroup.releaseConnection(connFuture.get());
                    }
                    return;
                }

                if (connFuture.isSuccess()) {
                    // 获取连接成功
                    Connection conn = connFuture.get();

                    if (checkInvalidOrNot(invocationPromise, conn)) {
                        return;
                    }

                    try {
                        // 设置超时定时器
                        invocationPromise.setTimeout(TimerHolder.getTimer()
                            .newTimeout(
                                timeout -> {
                                    // 请求已超时，移除
                                    // promise，返回超时异常
                                    InvocationPromise<?> p = conn.removePromise(req.getId());
                                    if (p != null) {
                                        p.setFailure(new TimeoutException(
                                            "Wait for response timeout. Request id:{}, To:{}",
                                            req.getId(), NetUtil.getRemoteAddress(conn.getChannel())));
                                    }
                                },
                                timeoutMillis,
                                TimeUnit.MILLISECONDS));

                        // 发送请求
                        conn.getChannel().writeAndFlush(req).addListener(future -> {
                            if (!future.isSuccess()) {
                                // 发送失败。移除 promise，并取消响应超时监听
                                InvocationPromise<?> p = conn.removePromise(req.getId());
                                if (p != null) {
                                    p.cancelTimeout();
                                    promise.setFailure(
                                        new TransportException(future.cause().getMessage(), future.cause()));
                                }

                                LOGGER.error(
                                    "Unexpected exception when write request. Request id:{}, To:{}",
                                    req.getId(),
                                    NetUtil.getRemoteAddress(conn.getChannel()),
                                    future.cause());
                            } else {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug(
                                        "Sent request. Request Id:{}, To:{}.",
                                        req.getId(),
                                        NetUtil.getRemoteAddress(conn.getChannel()));
                                }
                            }
                        });

                        // 添加 promise
                        conn.addPromise(req.getId(), invocationPromise);
                    } finally {
                        connectionGroup.releaseConnection(conn);
                    }
                } else {
                    // 获取连接失败
                    invocationPromise.setFailure(new TransportException(
                        "Failed to acquire connection. Request id:{}, To:{}",
                        req.getId(),
                        NetUtil.getRemoteAddress(url.getAddress()),
                        connFuture.cause()));
                }
            });

            //处理请求响应
            invocationPromise.addListener((GenericFutureListener<Future<ResponseCommand>>) future -> {
                if (future.isSuccess()) {
                    try {
                        ResponseCommand response = future.get();
                        if (response.getStatus() == RpcStatus.OK) {
                            if (response.getContent() != null && response.getContent().length > 0) {
                                promise.setSuccess((T) deserialize(response));
                            } else {
                                promise.setSuccess(null);
                            }
                        } else {
                            if (response.getContent() != null && response.getContent().length > 0) {
                                promise.setSuccess((T) deserialize(response));
                            } else {
                                promise.setFailure(new RemotingException(
                                    "Remoting server error. ResponseStatus: {}",
                                    response.getStatus().name()));
                            }
                        }
                    } catch (Throwable t) {
                        reportException(promise, t);
                    }
                } else {
                    reportException(promise, future.cause());
                }
            });
        } catch (Throwable t) {
            reportException(promise, t);
        }

        return promise;
    }

    private void reportException(Promise<?> promise, Throwable t) {
        if (t instanceof TransportException) {
            promise.setFailure(t);
        } else {
            promise.setFailure(new TransportException(t.getMessage(), t));
        }
    }

    /**
     * 检查连接是否不可用
     * @param promise promise
     * @param connection connection
     * @return {@code true}，连接不可用。{@code false}，连接可用。
     */
    private boolean checkInvalidOrNot(Promise<?> promise, Connection connection) {
        if (!connection.isActive()) {
            promise.setFailure(new TransportException("Connection is not active."));
            return true;
        }

        if (!connection.getChannel().isWritable()) {
            promise.setFailure(new TransportException("Channel has reached high water mark."));
            return true;
        }

        return false;
    }
}
