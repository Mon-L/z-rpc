package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.utils.NetUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class ConnectionEventHandler extends ChannelDuplexHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionEventHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.warn("Exception occurred. Remote address:" + NetUtil.getRemoteAddress(ctx.channel()), cause);
        ctx.channel().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
}
