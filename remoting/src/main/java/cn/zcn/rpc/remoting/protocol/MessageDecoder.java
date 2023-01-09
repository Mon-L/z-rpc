package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.Protocol;
import cn.zcn.rpc.remoting.ProtocolManager;
import cn.zcn.rpc.remoting.constants.AttributeKeys;
import cn.zcn.rpc.remoting.exception.ProtocolException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 * 用于解码协议字节码。首先解析协议码，根据协议码获取指定协议的解码器，然后使用解码器解析字节数组。
 *
 * @author zicung
 */
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() <= ProtocolCode.LENGTH) {
            return;
        }

        byteBuf.markReaderIndex();
        ProtocolCode protocolCode;
        try {
            protocolCode = ProtocolCode.from(byteBuf.readByte(), byteBuf.readByte());
            context.channel().attr(AttributeKeys.PROTOCOL).set(protocolCode);
        } finally {
            byteBuf.resetReaderIndex();
        }

        Protocol protocol = ProtocolManager.getInstance().getProtocol(protocolCode);

        if (protocol == null) {
            throw new ProtocolException("Unknown protocol : " + protocolCode);
        }

        protocol.getDecoder().decode(context, byteBuf, list);
    }
}
