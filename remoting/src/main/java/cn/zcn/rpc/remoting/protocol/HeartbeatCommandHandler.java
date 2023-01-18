package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.constants.AttributeKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zcn.rpc.remoting.CommandHandler;
import cn.zcn.rpc.remoting.InvocationPromise;
import cn.zcn.rpc.remoting.CommandContext;
import cn.zcn.rpc.remoting.connection.Connection;
import cn.zcn.rpc.remoting.utils.NetUtil;

/**
 * 处理 {@code HeartbeatCommand} 和 {@code HeartbeatAckCommand}。
 * <pre>
 * 1. 处理 {@code HeartbeatCommand}，并返回 {@code HeartbeatAckCommand}。
 * 2. 处理 {@code HeartbeatAckCommand}，给对应的 {@code InvocationPromise} 设置响应。
 * </pre>
 *
 * @author zicung
 */
public class HeartbeatCommandHandler implements CommandHandler<BaseCommand> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatCommandHandler.class);

    @Override
    public void handle(CommandContext context, BaseCommand command) {
        if (command instanceof HeartbeatCommand) {
            HeartbeatCommand heartbeatCommand = (HeartbeatCommand) command;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                    "Received heartbeat. Id:{}, From:{}",
                    heartbeatCommand.getId(),
                    NetUtil.getRemoteAddress(context.getChannelContext().channel()));
            }

            HeartbeatAckCommand heartbeatAckCommand = context.getProtocol().getCommandFactory()
                .createHeartbeatAckCommand(heartbeatCommand);

            context.getChannelContext().writeAndFlush(heartbeatAckCommand).addListener(future -> {
                if (future.isSuccess()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(
                            "Sent heartbeat ack successfully. Id:{}, To:{}",
                            heartbeatCommand.getId(),
                            NetUtil.getRemoteAddress(context.getChannelContext().channel()));
                    }
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(
                            "Failed to send heartbeat ack. Id:{}, To:{}",
                            heartbeatCommand.getId(),
                            NetUtil.getRemoteAddress(context.getChannelContext().channel()));
                    }
                }
            });
        } else if (command instanceof HeartbeatAckCommand) {
            HeartbeatAckCommand heartbeatAckCommand = (HeartbeatAckCommand) command;

            Connection connection = context.getChannelContext()
                .channel()
                .attr(AttributeKeys.CONNECTION)
                .get();

            InvocationPromise<ResponseCommand> future = connection.removePromise(heartbeatAckCommand.getId());
            if (future != null) {
                future.setSuccess(heartbeatAckCommand);
                future.cancelTimeout();
            } else {
                LOGGER.warn(
                    "Cannot find heartbeat invokeFuture. Id:{}, From:{}",
                    heartbeatAckCommand.getId(),
                    NetUtil.getRemoteAddress(context.getChannelContext().channel()));
            }
        } else {
            throw new IllegalArgumentException(
                "Can not process command , command class [" + command.getClass().getName() + "]");
        }
    }
}
