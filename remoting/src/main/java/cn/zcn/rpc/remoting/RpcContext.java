package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.config.Options;
import cn.zcn.rpc.remoting.protocol.ICommand;
import cn.zcn.rpc.remoting.serialization.Serializer;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcInboundHandler.class);

    private final Options options;
    private final ChannelHandlerContext channelContext;
    private final RequestProcessor requestProcessor;
    private final Protocol protocol;

    private Serializer serializer;

    public RpcContext(Options options, ChannelHandlerContext channelContext, Protocol protocol, RequestProcessor requestProcessor) {
        this.options = options;
        this.channelContext = channelContext;
        this.protocol = protocol;
        this.requestProcessor = requestProcessor;
    }

    public Options getOptions() {
        return options;
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

    public RequestProcessor getRpcProcessor() {
        return requestProcessor;
    }

    public void writeAndFlush(ICommand response) {
        channelContext.writeAndFlush(response).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                LOGGER.error("Failed to send response. Request id:{}", response.getId());
            } else {
                LOGGER.error("Sent response. Request id:{}", response.getId());
            }
        });
    }
}
