package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.exception.ProtocolException;
import cn.zcn.rpc.remoting.Protocol;
import cn.zcn.rpc.remoting.ProtocolManager;
import cn.zcn.rpc.remoting.protocol.v1.RpcProtocolV1;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.Serializable;


/**
 * 用于编码协议。
 * 根据协议码获取指定协议的编码器，使用该编码器编码协议。
 */
public class MessageEncoder extends MessageToByteEncoder<Serializable> {

    private static final ProtocolCode DEFAULT_PROTOCOL = RpcProtocolV1.PROTOCOL_CODE;
    private final ProtocolManager protocolManager;

    public MessageEncoder(ProtocolManager protocolManager) {
        super();
        this.protocolManager = protocolManager;
    }

    @Override
    protected void encode(ChannelHandlerContext context, Serializable msg, ByteBuf byteBuf) throws Exception {
        ProtocolCode protocolCode = context.channel().attr(Protocol.PROTOCOL).get();
        if (protocolCode == null) {
            protocolCode = DEFAULT_PROTOCOL;
        }

        Protocol protocol = protocolManager.getProtocol(protocolCode);

        if (protocol == null) {
            throw new ProtocolException("Unknown protocol : " + protocolCode);
        }

        protocol.getEncoder().encode(context, msg, byteBuf);
    }
}
