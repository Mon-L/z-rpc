package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.utils.NetUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理连接事件
 *
 * @author zicung
 */
@ChannelHandler.Sharable
public class ConnectionEventHandler extends ChannelDuplexHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionEventHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.warn("Exception occurred. Remote address:" + NetUtil.getRemoteAddress(ctx.channel()), cause);
        ctx.channel().close();
    }
}
