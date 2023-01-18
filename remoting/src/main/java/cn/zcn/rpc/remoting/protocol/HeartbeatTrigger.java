package cn.zcn.rpc.remoting.protocol;

import io.netty.channel.ChannelHandlerContext;

/**
 * 心跳事件处理器，处理心跳事件。
 * 
 * @author zicung
 */
public interface HeartbeatTrigger {
	/**
	 * 处理心跳事件
	 * 
	 * @param ctx
	 *            {@link ChannelHandlerContext}
	 */
	void heartbeatTriggered(ChannelHandlerContext ctx);
}
