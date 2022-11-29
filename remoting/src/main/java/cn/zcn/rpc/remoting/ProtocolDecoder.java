package cn.zcn.rpc.remoting;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * 协议解码器，解码协议字节码
 */
public interface ProtocolDecoder {

    /**
     * 解码
     *
     * @param context ChannelHandlerContext
     * @param byteBuf ByteBuf
     * @param out     out
     * @throws Exception 解码失败
     */
    void decode(ChannelHandlerContext context, ByteBuf byteBuf, List<Object> out) throws Exception;
}
