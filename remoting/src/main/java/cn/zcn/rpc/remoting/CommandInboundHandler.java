package cn.zcn.rpc.remoting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zcn.rpc.remoting.constants.AttributeKeys;
import cn.zcn.rpc.remoting.protocol.CommandType;
import cn.zcn.rpc.remoting.protocol.ICommand;
import cn.zcn.rpc.remoting.protocol.ProtocolCode;
import cn.zcn.rpc.remoting.protocol.RpcStatus;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * {@link ICommand} 入站处理器，根据 {@code ICommand} 的 {@code Protocol} 获取 {@link CommandHandler} 进行处理。
 *
 * @author zicung
 */
@ChannelHandler.Sharable
class CommandInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandInboundHandler.class);

    private final RequestCommandDispatcher requestCommandDispatcher;

    CommandInboundHandler(RequestCommandDispatcher requestCommandDispatcher) {
        this.requestCommandDispatcher = requestCommandDispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext channelContext, Object msg) {
        ProtocolCode protocolCode = channelContext.channel().attr(AttributeKeys.PROTOCOL).get();
        Protocol protocol = ProtocolManager.getInstance().getProtocol(protocolCode);

        CommandContext commandContext = new CommandContext(channelContext, protocol, requestCommandDispatcher);
        ICommand request = null;
        try {
            request = (ICommand) msg;

            CommandHandler<ICommand> commandHandler = protocol.getCommandHandler(request.getCommandCode());
            if (commandHandler == null) {
                writeAndFlushWithRpcStatus(commandContext, protocol, request, RpcStatus.UNSUPPORTED_COMMAND);
                return;
            }

            commandHandler.handle(commandContext, request);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);

            writeAndFlushWithRpcStatus(commandContext, protocol, request, RpcStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 发送包含指定 {@code RpcStatus} 的 {@code ResponseCommand}
     *
     * @param protocol 协议
     * @param request 请求
     * @param rpcStatus 响应码
     */
    private void writeAndFlushWithRpcStatus(CommandContext commandContext, Protocol protocol, ICommand request,
                                            RpcStatus rpcStatus) {
        if (request.getCommandType() != CommandType.REQUEST_ONEWAY) {
            commandContext.writeAndFlush(protocol.getCommandFactory().createResponseCommand(request, rpcStatus));
        }
    }
}
