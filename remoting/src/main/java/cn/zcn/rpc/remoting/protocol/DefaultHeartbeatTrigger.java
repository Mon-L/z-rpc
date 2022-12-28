package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.DefaultInvokePromise;
import cn.zcn.rpc.remoting.InvokePromise;
import cn.zcn.rpc.remoting.config.ClientOptions;
import cn.zcn.rpc.remoting.connection.Connection;
import cn.zcn.rpc.remoting.exception.TimeoutException;
import cn.zcn.rpc.remoting.utils.NetUtil;
import cn.zcn.rpc.remoting.utils.TimerHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class DefaultHeartbeatTrigger implements HeartbeatTrigger {
    final static int HEARTBEAT_TIMEOUT_MILLIS = 1000;

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultHeartbeatTrigger.class);
    private final CommandFactory commandFactory;

    public DefaultHeartbeatTrigger(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @Override
    public void heartbeatTriggered(ChannelHandlerContext ctx) {
        Connection conn = ctx.channel().attr(Connection.CONNECTION_KEY).get();
        if (conn == null) {
            return;
        }

        int maxFailures = conn.getOption(ClientOptions.HEARTBEAT_MAX_FAILURES);
        if (conn.getHeartbeatFailures() >= maxFailures) {
            conn.close();
            LOGGER.warn("Heartbeat timeout, close channel. Remoting address:{}, Timeout times:{}",
                    NetUtil.getRemoteAddress(conn.getChannel()), conn.getHeartbeatFailures());
            return;
        }

        HeartbeatCommand heartbeatCommand = commandFactory.createHeartbeatCommand();
        InvokePromise<ResponseCommand> promise = new DefaultInvokePromise(ctx.executor().newPromise());
        promise.addListener((GenericFutureListener<Future<ResponseCommand>>) future -> {
            promise.cancelTimeout();

            if (future.isSuccess()) {
                ResponseCommand responseCommand = future.get();
                if (responseCommand.getStatus() == RpcStatus.OK) {
                    conn.setHeartbeatFailures(0);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Received heartbeat ack.Remoting address:{}", NetUtil.getRemoteAddress(ctx.channel()));
                    }
                } else {
                    conn.setHeartbeatFailures(conn.getHeartbeatFailures() + 1);

                    LOGGER.debug("Received heartbeat ack but error. Error status:{}, Remoting address:{}",
                            responseCommand.getStatus().name(), NetUtil.getRemoteAddress(ctx.channel()));
                }
            } else {
                conn.setHeartbeatFailures(conn.getHeartbeatFailures() + 1);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception occurred heartbeat ack. Error Msg:{}, Remoting address:{}",
                            future.cause().getMessage(), NetUtil.getRemoteAddress(ctx.channel()));
                }
            }
        });

        promise.setTimeout(TimerHolder.getTimer().newTimeout(timeout -> {
            InvokePromise<ResponseCommand> p = conn.removePromise(heartbeatCommand.getId());
            if (p != null) {
                p.setFailure(new TimeoutException("Wait for heartbeat ack timeout. Request id:{0}, Remoting address:{1}",
                        heartbeatCommand.getId(), NetUtil.getRemoteAddress(ctx.channel())));
            }
        }, HEARTBEAT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));

        conn.addPromise(heartbeatCommand.getId(), promise);

        // send heartbeat
        ctx.channel().writeAndFlush(heartbeatCommand).addListener((GenericFutureListener<Future<Void>>) future -> {
            if (!future.isSuccess()) {
                //发送失败，移除 promise、timeout
                InvokePromise<ResponseCommand> p = conn.removePromise(heartbeatCommand.getId());
                p.cancelTimeout();

                LOGGER.error("Failed to send heartbeat. Remoting address:{}", NetUtil.getRemoteAddress(ctx.channel()));
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Sent heartbeat.Remoting address:{}", NetUtil.getRemoteAddress(ctx.channel()));
                }
            }
        });
    }
}
