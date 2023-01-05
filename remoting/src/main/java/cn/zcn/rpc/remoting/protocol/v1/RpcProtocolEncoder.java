package cn.zcn.rpc.remoting.protocol.v1;

import cn.zcn.rpc.remoting.ProtocolEncoder;
import cn.zcn.rpc.remoting.exception.ProtocolException;
import cn.zcn.rpc.remoting.protocol.BaseCommand;
import cn.zcn.rpc.remoting.protocol.RequestCommand;
import cn.zcn.rpc.remoting.protocol.ResponseCommand;
import cn.zcn.rpc.remoting.utils.Crc32Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * {@link RpcProtocolV1} 编码器
 *
 * @author zicung
 */
public class RpcProtocolEncoder implements ProtocolEncoder {

    @Override
    public void encode(ChannelHandlerContext context, Object msg, ByteBuf out) throws Exception {
        if (!(msg instanceof BaseCommand)) {
            throw new ProtocolException("Message is not a instance of BaseCommand!");
        }

        int startIndex = out.writerIndex();

        BaseCommand command = (BaseCommand) msg;

        out.writeByte(command.getProtocolCode().getCode());
        out.writeByte(command.getProtocolCode().getVersion());
        out.writeShort(command.getCommandType().getValue());
        out.writeShort(command.getCommandCode().getValue());
        out.writeInt(command.getId());
        out.writeByte(command.getSerializer());
        out.writeByte(command.getProtocolSwitch().toByte());

        if (msg instanceof RequestCommand) {
            RequestCommand requestCommand = (RequestCommand) command;
            out.writeInt(requestCommand.getTimeout());
        } else if (msg instanceof ResponseCommand) {
            ResponseCommand responseCommand = (ResponseCommand) command;
            out.writeShort(responseCommand.getStatus().getValue());
        }

        if (command.getClazz() != null) {
            out.writeShort(command.getClazz().length);
        } else {
            out.writeShort(0);
        }

        if (command.getContent() != null) {
            out.writeInt(command.getContent().length);
        } else {
            out.writeInt(0);
        }

        if (command.getClazz() != null) {
            out.writeBytes(command.getClazz());
        }

        if (command.getContent() != null) {
            out.writeBytes(command.getContent());
        }

        if (command.getProtocolSwitch().isOn(0)) {
            // CRC32
            int endIndex = out.writerIndex();
            byte[] bytes = new byte[endIndex - startIndex];
            out.getBytes(startIndex, bytes, 0, bytes.length);
            int crc32 = Crc32Util.calculate(bytes);
            out.writeInt(crc32);
        }
    }
}
