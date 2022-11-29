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
import cn.zcn.rpc.remoting.utils.IDGenerator;
import cn.zcn.rpc.remoting.utils.NamedThreadFactory;
import cn.zcn.rpc.remoting.utils.NetUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RemotingInvoker extends AbstractLifecycle {

    private final static Logger LOGGER = LoggerFactory.getLogger(RemotingInvoker.class);

    private final Options options;
    private final EventExecutor eventExecutor;
    private final ProtocolProvider protocolManager;
    private final SerializerProvider serializerManager;
    private final Bootstrap bootstrap;

    private Timer timer;
    private ConnectionGroupManager connectionGroupManager;

    public RemotingInvoker(Options options, ProtocolProvider protocolProvider, SerializerProvider serializerProvider, Bootstrap bootstrap) {
        this.options = options;
        this.protocolManager = protocolProvider;
        this.serializerManager = serializerProvider;
        this.bootstrap = bootstrap;
        this.eventExecutor = bootstrap.config().group().next();
    }

    @Override
    protected void doStart() throws LifecycleException {
        this.connectionGroupManager = new ConnectionGroupManager(bootstrap);
        this.connectionGroupManager.start();

        this.timer = new HashedWheelTimer(new NamedThreadFactory("remoting-invoker-timer"), 10,
                TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doStop() throws LifecycleException {
        if (connectionGroupManager != null) {
            this.connectionGroupManager.stop();
        }

        if (this.timer != null) {
            this.timer.stop();
        }
    }

    /**
     * 距离超时还剩多少时间
     *
     * @param start   开始时间
     * @param timeout 超时时间
     * @return 剩余时间
     */
    private long getRemainingTime(long start, long timeout) {
        return start + timeout - System.currentTimeMillis();
    }

    /**
     * 创建请求
     *
     * @param payload       payload
     * @param timeoutMillis 请求超时时间
     * @param commandType   请求类型
     * @return ICommand
     */
    private ICommand createRequestCommand(Object payload, int timeoutMillis, CommandType commandType) {
        Protocol protocol = protocolManager.getDefaultProtocol();
        CommandFactory commandFactory = protocol.getCommandFactory();
        RequestCommand req = commandFactory.createRequestCommand(commandType, CommandCode.REQUEST);

        req.setId(IDGenerator.getInstance().nextId());
        req.setTimeout(timeoutMillis);

        byte[] clazz = payload.getClass().getName().getBytes(options.getOption(ClientOptions.CHARSET));
        req.setClazz(clazz);

        byte serializer = serializerManager.getDefaultSerializerCode();
        req.setSerializer(serializer);
        req.setContent(serializerManager.getSerializer(serializer).serialize(payload));

        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 0);
        if (options.getOption(ClientOptions.USE_CRC32)) {
            protocolSwitch.turnOn(0);
        }
        req.setProtocolSwitch(protocolSwitch);
        return req;
    }

    private Object deserialize(ResponseCommand responseCommand) throws SerializationException {
        Serializer serializer = serializerManager.getSerializer(responseCommand.getSerializer());
        if (serializer == null) {
            throw new SerializationException("Unknown serializer with " + responseCommand.getSerializer());
        }

        return serializer.deserialize(responseCommand.getContent(), new String(responseCommand.getClazz(), options.getOption(ClientOptions.CHARSET)));
    }

    /**
     * 发送单向请求，这种类型的请求不会有响应结果
     *
     * @param url 请求路径
     * @param obj 请求体
     */
    public Future<Void> oneWayInvoke(Url url, Object obj) {
        if (!isStarted()) {
            return eventExecutor.newFailedFuture(new IllegalStateException("RemotingInvoker is closed."));
        }

        Promise<Void> promise = eventExecutor.newPromise();

        try {
            //创建请求
            ICommand req = createRequestCommand(obj, -1, CommandType.REQUEST_ONEWAY);

            //获取连接组
            ConnectionGroup connectionGroup = connectionGroupManager.getConnectionGroup(url);

            connectionGroup.acquireConnection().addListener((GenericFutureListener<Future<Connection>>) connFuture -> {
                if (connFuture.isSuccess()) {
                    Connection conn = connFuture.get();
                    try {
                        conn.getChannel().writeAndFlush(req).addListener(future -> {
                            if (!future.isSuccess()) {
                                LOGGER.error("Unexpected exception when write request. Request id:{}, Remoting address:{}", req.getId(),
                                        NetUtil.getRemoteAddress(conn.getChannel()), future.cause());
                                promise.setFailure(future.cause());
                            } else {
                                promise.setSuccess(null);
                            }
                        });
                    } finally {
                        connectionGroup.releaseConnection(conn);
                    }
                } else {
                    promise.setFailure(new TransportException(connFuture.cause(), "Failed to acquire connection. Request id:{0}, Remoting address:{1}",
                            req.getId(), NetUtil.getRemoteAddress(url.getAddress())));
                }
            });

        } catch (Throwable t) {
            promise.setFailure(t);
        }

        return promise;
    }

    /**
     * 发送具有响应结果的请求
     *
     * @param url           请求路径
     * @param obj           请求体
     * @param timeoutMillis 响应超时时间
     */
    @SuppressWarnings({"unchecked"})
    public <T> Future<T> invoke(Url url, Object obj, int timeoutMillis) {
        if (!isStarted()) {
            return eventExecutor.newFailedFuture(new IllegalStateException("RemotingInvoker is closed."));
        }

        long startMillis = System.currentTimeMillis();
        Promise<T> promise = eventExecutor.newPromise();

        try {
            //创建请求体
            ICommand req = createRequestCommand(obj, timeoutMillis, CommandType.REQUEST);

            //获取连接组
            ConnectionGroup connectionGroup = connectionGroupManager.getConnectionGroup(url);

            InvokePromise<ResponseCommand> invokePromise = new DefaultInvokePromise(eventExecutor.newPromise());
            invokePromise.addListener((GenericFutureListener<Future<ResponseCommand>>) future -> {
                if (future.isSuccess()) {
                    try {
                        ResponseCommand response = future.get();
                        if (response.getStatus() == RpcStatus.OK) {
                            promise.setSuccess((T) deserialize(response));
                        } else {
                            //TODO get cause from response
                            promise.setFailure(new RemotingException("Remoting server error."));
                        }
                    } catch (Throwable t) {
                        promise.setFailure(t);
                    }
                } else {
                    promise.setFailure(future.cause());
                }
            });

            //判断请求是否已超时
            if (getRemainingTime(startMillis, timeoutMillis) <= 0) {
                invokePromise.setFailure(new TimeoutException("Send request timeout. Request id:{0}, Remoting address:{1}",
                        req.getId(), NetUtil.getRemoteAddress(url.getAddress())));
                return promise;
            }

            connectionGroup.acquireConnection().addListener((GenericFutureListener<Future<Connection>>) connFuture -> {
                long remainingTime = getRemainingTime(startMillis, timeoutMillis);
                if (remainingTime <= 0) {
                    //请求已超时，返回超时异常
                    invokePromise.setFailure(new TimeoutException("Send request timeout. Request id:{0}, Remoting address:{1}",
                            req.getId(), NetUtil.getRemoteAddress(url.getAddress())));

                    //释放连接
                    if (connFuture.isSuccess()) {
                        connectionGroup.releaseConnection(connFuture.get());
                    }
                    return;
                }

                if (connFuture.isSuccess()) {
                    Connection conn = connFuture.get();
                    try {
                        invokePromise.setTimeout(timer.newTimeout(timeout -> {
                            /*
                             * 请求已超时，移除 promise，返回超时异常
                             */
                            InvokePromise<?> p = conn.removePromise(req.getId());
                            if (p != null) {
                                p.setFailure(new TimeoutException("Wait for response timeout. Request id:{0}, Remoting address:{1}",
                                        req.getId(), NetUtil.getRemoteAddress(conn.getChannel())));
                            }
                        }, timeoutMillis, TimeUnit.MILLISECONDS));

                        //添加 promise
                        conn.addPromise(req.getId(), invokePromise);

                        conn.getChannel().writeAndFlush(req).addListener(future -> {
                            if (!future.isSuccess()) {
                                /*
                                 * 发送失败。移除 promise，并取消响应超时监听
                                 */
                                InvokePromise<?> p = conn.removePromise(req.getId());
                                if (p != null) {
                                    p.cancelTimeout();
                                    p.setFailure(future.cause());
                                }

                                LOGGER.error("Unexpected exception when write request. Request id:{}, Remoting address:{}", req.getId(),
                                        NetUtil.getRemoteAddress(conn.getChannel()), future.cause());
                            }
                        });
                    } finally {
                        connectionGroup.releaseConnection(conn);
                    }
                } else {
                    invokePromise.setFailure(new TransportException(connFuture.cause(), "Failed to acquire connection. Request id:{0}, Remoting address{1}",
                            req.getId(), NetUtil.getRemoteAddress(url.getAddress())));
                }
            });
        } catch (Throwable t) {
            promise.setFailure(t);
        }

        return promise;
    }
}
