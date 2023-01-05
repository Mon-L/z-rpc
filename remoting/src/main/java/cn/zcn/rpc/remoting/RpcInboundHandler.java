package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.ICommand;
import cn.zcn.rpc.remoting.protocol.ProtocolCode;
import cn.zcn.rpc.remoting.protocol.ResponseCommand;
import cn.zcn.rpc.remoting.protocol.RpcStatus;
import cn.zcn.rpc.remoting.serialization.Serializer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息入站处理器，将 {@link ICommand} 按协议分发到不同的 {@link CommandHandler} 进行处理
 *
 * @author zicung
 */
@ChannelHandler.Sharable
class RpcInboundHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcInboundHandler.class);

    private final RequestDispatcher requestDispatcher;

    RpcInboundHandler(RequestDispatcher requestDispatcher) {
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext channelContext, Object msg) {
        ProtocolCode protocolCode = channelContext.channel().attr(Protocol.PROTOCOL).get();
        Protocol protocol = ProtocolManager.getInstance().getProtocol(protocolCode);

        RpcContext rpcContext = new RpcContext(channelContext, protocol, requestDispatcher);
        ICommand command = null;
        try {
            command = (ICommand) msg;

            Serializer serializer = SerializerManager.getInstance().getSerializer(command.getSerializer());
            if (serializer != null) {
                rpcContext.setSerializer(serializer);
            } else {
                rpcContext.writeAndFlush(createResponseCommand(protocol, command, RpcStatus.UNSUPPORTED_SERIALIZER));
                return;
            }

            CommandHandler<ICommand> commandHandler = protocol.getCommandHandler(command.getCommandCode());
            if (commandHandler == null) {
                rpcContext.writeAndFlush(createResponseCommand(protocol, command, RpcStatus.UNSUPPORTED_COMMAND));
                return;
            }

            commandHandler.handle(rpcContext, command);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);

            if (command != null) {
                rpcContext.writeAndFlush(createResponseCommand(protocol, command, RpcStatus.INTERNAL_SERVER_ERROR));
            }
        }
    }

    /**
     * 创建响应
     *
     * @param protocol       协议
     * @param requestCommand 请求
     * @param rpcStatus      响应码
     * @return 响应
     */
    private ResponseCommand createResponseCommand(Protocol protocol, ICommand requestCommand, RpcStatus rpcStatus) {
        return protocol.getCommandFactory().createResponseCommand(requestCommand, rpcStatus);
    }
}
