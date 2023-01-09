package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.DefaultInvocationPromise;
import cn.zcn.rpc.remoting.InvocationPromise;
import cn.zcn.rpc.remoting.config.ClientOptions;
import cn.zcn.rpc.remoting.connection.Connection;
import cn.zcn.rpc.remoting.constants.AttributeKeys;
import cn.zcn.rpc.remoting.exception.TimeoutException;
import cn.zcn.rpc.remoting.utils.NetUtil;
import cn.zcn.rpc.remoting.utils.TimerHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default HeartbeatTrigger
 *
 * @author zicung
 */
public class DefaultHeartbeatTrigger implements HeartbeatTrigger {
    static final int HEARTBEAT_TIMEOUT_MILLIS = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHeartbeatTrigger.class);
    private final CommandFactory commandFactory;

    public DefaultHeartbeatTrigger(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @Override
    public void heartbeatTriggered(ChannelHandlerContext ctx) {
        Connection conn = ctx.channel().attr(AttributeKeys.CONNECTION).get();
        if (conn == null) {
            return;
        }

        int maxFailures = conn.getOption(ClientOptions.HEARTBEAT_MAX_FAILURES);
        if (conn.getHeartbeatFailures() >= maxFailures) {
            conn.close();
            LOGGER.warn(
                "Heartbeat timeout, close channel. Remoting address:{}, Timeout times:{}",
                NetUtil.getRemoteAddress(conn.getChannel()),
                conn.getHeartbeatFailures());
            return;
        }

        HeartbeatCommand heartbeatCommand = commandFactory.createHeartbeatCommand();
        InvocationPromise<ResponseCommand> promise = new DefaultInvocationPromise(ctx.executor().newPromise());
        promise.addListener((GenericFutureListener<Future<ResponseCommand>>) future -> {
            promise.cancelTimeout();

            if (future.isSuccess()) {
                ResponseCommand responseCommand = future.get();
                if (responseCommand.getStatus() == RpcStatus.OK) {
                    conn.setHeartbeatFailures(0);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(
                            "Received heartbeat ack. Id:{}, From:{}",
                            heartbeatCommand.getId(),
                            NetUtil.getRemoteAddress(ctx.channel()));
                    }
                } else {
                    conn.setHeartbeatFailures(conn.getHeartbeatFailures() + 1);

                    LOGGER.debug(
                        "Received heartbeat ack but error. Error status:{}, Id:{}, From:{}",
                        responseCommand.getStatus().name(),
                        heartbeatCommand.getId(),
                        NetUtil.getRemoteAddress(ctx.channel()));
                }
            } else {
                conn.setHeartbeatFailures(conn.getHeartbeatFailures() + 1);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                        "Exception occurred heartbeat ack. Error Msg:{}, Id:{}, To:{}",
                        future.cause().getMessage(),
                        heartbeatCommand.getId(),
                        NetUtil.getRemoteAddress(ctx.channel()));
                }
            }
        });

        promise.setTimeout(TimerHolder.getTimer()
            .newTimeout(
                timeout -> {
                    InvocationPromise<ResponseCommand> p = conn.removePromise(heartbeatCommand.getId());
                    if (p != null) {
                        p.setFailure(new TimeoutException(
                            "Wait for heartbeat ack timeout. Id:{0}, To:{1}",
                            heartbeatCommand.getId(), NetUtil.getRemoteAddress(ctx.channel())));
                    }
                },
                HEARTBEAT_TIMEOUT_MILLIS,
                TimeUnit.MILLISECONDS));

        conn.addPromise(heartbeatCommand.getId(), promise);

        // send heartbeat
        ctx.channel().writeAndFlush(heartbeatCommand).addListener((GenericFutureListener<Future<Void>>) future -> {
            if (!future.isSuccess()) {
                // 发送失败，移除 promise、timeout
                InvocationPromise<ResponseCommand> p = conn.removePromise(heartbeatCommand.getId());
                p.cancelTimeout();

                // 心跳失败次数加一
                conn.setHeartbeatFailures(conn.getHeartbeatFailures() + 1);

                LOGGER.error(
                    "Failed to send heartbeat. Id:{}, To:{}",
                    heartbeatCommand.getId(),
                    NetUtil.getRemoteAddress(ctx.channel()));
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                        "Sent heartbeat. Id:{}, To:{}",
                        heartbeatCommand.getId(),
                        NetUtil.getRemoteAddress(ctx.channel()));
                }
            }
        });
    }
}
