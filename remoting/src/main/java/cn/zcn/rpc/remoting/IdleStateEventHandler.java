package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.ProtocolCode;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

@ChannelHandler.Sharable
public class IdleStateEventHandler extends ChannelInboundHandlerAdapter {

    private final ProtocolProvider protocolProvider;

    public IdleStateEventHandler(ProtocolProvider protocolProvider) {
        this.protocolProvider = protocolProvider;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ProtocolCode protocolCode = ctx.channel().attr(Protocol.PROTOCOL).get();
            Protocol protocol = protocolProvider.getProtocol(protocolCode);
            protocol.getHeartbeatTrigger().heartbeatTriggered(ctx);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
