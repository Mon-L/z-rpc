package cn.zcn.rpc.remoting;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 协议编码器，将协议编码成字节码
 *
 * @author zicung
 */
public interface ProtocolEncoder {

    /**
     * 编码
     *
     * @param context ChannelHandlerContext
     * @param msg     message
     * @param byteBuf ByteBuf
     * @throws Exception 编码失败
     */
    void encode(ChannelHandlerContext context, Object msg, ByteBuf byteBuf) throws Exception;
}
