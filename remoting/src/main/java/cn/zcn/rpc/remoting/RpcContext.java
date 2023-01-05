package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.ICommand;
import cn.zcn.rpc.remoting.serialization.Serializer;
import cn.zcn.rpc.remoting.utils.NetUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rpc invocation context.
 *
 * @author zicung
 */
public class RpcContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcInboundHandler.class);

    private final ChannelHandlerContext channelContext;
    private final RequestDispatcher requestDispatcher;
    private final Protocol protocol;

    private Serializer serializer;

    public RpcContext(ChannelHandlerContext channelContext, Protocol protocol, RequestDispatcher requestDispatcher) {
        this.channelContext = channelContext;
        this.protocol = protocol;
        this.requestDispatcher = requestDispatcher;
    }

    public ChannelHandlerContext getChannelContext() {
        return channelContext;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public RequestDispatcher getRequestDispatcher() {
        return requestDispatcher;
    }

    public void writeAndFlush(ICommand response) {
        channelContext.channel().writeAndFlush(response).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                LOGGER.error("Failed to send response. Request id:{}, To:{}", response.getId(), NetUtil.getRemoteAddress(channelContext.channel()));
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Sent response. Request id:{}, To:{}", response.getId(), NetUtil.getRemoteAddress(channelContext.channel()));
                }
            }
        });
    }
}
