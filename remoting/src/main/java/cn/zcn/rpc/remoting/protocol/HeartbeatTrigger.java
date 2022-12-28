package cn.zcn.rpc.remoting.protocol;

import io.netty.channel.ChannelHandlerContext;

public interface HeartbeatTrigger {
    void heartbeatTriggered(ChannelHandlerContext ctx);
}
