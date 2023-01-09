package cn.zcn.rpc.remoting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zcn.rpc.remoting.protocol.ICommand;
import cn.zcn.rpc.remoting.utils.NetUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

/**
 * Rpc invocation context.
 *
 * @author zicung
 */
public class CommandContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandContext.class);

    private final Protocol protocol;
    private final ChannelHandlerContext channelContext;
    private final RequestCommandDispatcher requestCommandDispatcher;

    public CommandContext(ChannelHandlerContext channelContext, Protocol protocol,
                          RequestCommandDispatcher requestCommandDispatcher) {
        this.channelContext = channelContext;
        this.protocol = protocol;
        this.requestCommandDispatcher = requestCommandDispatcher;
    }

    public ChannelHandlerContext getChannelContext() {
        return channelContext;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public RequestCommandDispatcher getRequestCommandDispatcher() {
        return requestCommandDispatcher;
    }

    public void writeAndFlush(ICommand response) {
        channelContext.channel().writeAndFlush(response).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                LOGGER.error(
                    "Failed to send response. Request id:{}, To:{}",
                    response.getId(),
                    NetUtil.getRemoteAddress(channelContext.channel()));
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                        "Sent response. Request id:{}, To:{}",
                        response.getId(),
                        NetUtil.getRemoteAddress(channelContext.channel()));
                }
            }
        });
    }
}
