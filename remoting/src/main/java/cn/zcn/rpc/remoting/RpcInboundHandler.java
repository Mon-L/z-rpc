package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.config.Options;
import cn.zcn.rpc.remoting.config.RpcOptions;
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
 * 将 {@link ICommand} 按协议分发到不同的 {@link CommandHandler} 进行处理
 */
@ChannelHandler.Sharable
class RpcInboundHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcInboundHandler.class);

    private final Options options;
    private final ProtocolProvider protocolProvider;
    private final SerializerProvider serializerProvider;
    private final RequestProcessor requestProcessor;

    RpcInboundHandler(RpcOptions options, ProtocolProvider protocolProvider, SerializerProvider serializerProvider, RequestProcessor requestProcessor) {
        this.options = options;
        this.protocolProvider = protocolProvider;
        this.serializerProvider = serializerProvider;
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext channelContext, Object msg) {
        ProtocolCode protocolCode = channelContext.channel().attr(Protocol.PROTOCOL).get();
        Protocol protocol = protocolProvider.getProtocol(protocolCode);

        RpcContext rpcContext = new RpcContext(options, channelContext, protocol, requestProcessor);
        ICommand command = null;
        try {
            command = (ICommand) msg;

            Serializer serializer = serializerProvider.getSerializer(command.getSerializer());
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

            channelContext.fireChannelRead(msg);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);

            if (command != null) {
                rpcContext.writeAndFlush(createResponseCommand(protocol, command, RpcStatus.INTERNAL_SERVER_ERROR));
            }
        }
    }

    private ResponseCommand createResponseCommand(Protocol protocol, ICommand requestCommand, RpcStatus rpcStatus) {
        return protocol.getCommandFactory().createResponseCommand(requestCommand, rpcStatus);
    }
}
