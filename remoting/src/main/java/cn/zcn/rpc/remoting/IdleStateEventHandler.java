package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.ProtocolCode;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 连接空闲事件的处理器
 *
 * @author zicung
 */
@ChannelHandler.Sharable
public class IdleStateEventHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdleStateEventHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ProtocolCode protocolCode = ctx.channel().attr(Protocol.PROTOCOL).get();

            Protocol protocol;
            if (protocolCode != null) {
                protocol = ProtocolManager.getInstance().getProtocol(protocolCode);
            } else {
                protocol = ProtocolManager.getInstance().getDefaultProtocol();
                LOGGER.info("Can not get protocol code from channel, use default protocol. Default protocol:{}", protocol.getProtocolCode());
            }

            protocol.getHeartbeatTrigger().heartbeatTriggered(ctx);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
