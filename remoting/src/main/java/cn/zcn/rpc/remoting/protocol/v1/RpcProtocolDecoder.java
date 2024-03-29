package cn.zcn.rpc.remoting.protocol.v1;

import cn.zcn.rpc.remoting.ProtocolDecoder;
import cn.zcn.rpc.remoting.exception.ProtocolException;
import cn.zcn.rpc.remoting.protocol.*;
import cn.zcn.rpc.remoting.utils.Crc32Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * {@link RpcProtocolV1} 解码器
 *
 * @author zicung
 */
public class RpcProtocolDecoder implements ProtocolDecoder {

    @Override
    public void decode(ChannelHandlerContext context, ByteBuf byteBuf, List<Object> out) throws Exception {
        if (byteBuf.readableBytes() < RpcProtocolV1.MIN_MESSAGE_LENGTH) {
            return;
        }

        int startIndex = byteBuf.readerIndex();
        byteBuf.markReaderIndex();

        ProtocolCode protocolCode = ProtocolCode.from(byteBuf.readByte(), byteBuf.readByte());
        if (!RpcProtocolV1.PROTOCOL_CODE.equals(protocolCode)) {
            throw new ProtocolException(
                "Excepted protocol {}, but got {}.", RpcProtocolV1.PROTOCOL_CODE, protocolCode);
        }

        short type = byteBuf.readShort();

        if (type == CommandType.REQUEST.getValue() || type == CommandType.REQUEST_ONEWAY.getValue()) {
            if (byteBuf.readableBytes() < RpcProtocolV1.MIN_REQUEST_LENGTH - 4) {
                byteBuf.resetReaderIndex();
                return;
            }

            CommandCode commandCode = CommandCode.valueOf(byteBuf.readShort());

            RequestCommand command;
            if (commandCode == CommandCode.HEARTBEAT) {
                command = new HeartbeatCommand(protocolCode);
            } else {
                command = new RequestCommand(protocolCode, CommandType.valueOf(type));
            }

            command.setCommandCode(commandCode);
            command.setId(byteBuf.readInt());
            command.setSerializer(byteBuf.readByte());
            command.setProtocolSwitch(ProtocolSwitch.parse(byteBuf.readByte()));
            command.setTimeout(byteBuf.readInt());

            short clazzLength = byteBuf.readShort();
            int contentLength = byteBuf.readInt();

            int requiredLength = clazzLength + contentLength;

            boolean hasCrc32 = command.getProtocolSwitch().isOn(0);
            if (hasCrc32) {
                requiredLength += 4;
            }

            if (byteBuf.readableBytes() >= requiredLength) {
                if (clazzLength > 0) {
                    byte[] clazz = new byte[clazzLength];
                    byteBuf.readBytes(clazz);
                    command.setClazz(clazz);
                }

                if (contentLength > 0) {
                    byte[] content = new byte[contentLength];
                    byteBuf.readBytes(content);
                    command.setContent(content);
                }

                if (hasCrc32) {
                    checkCrc32(byteBuf, startIndex);
                }

                out.add(command);
            } else {
                byteBuf.resetReaderIndex();
            }
        } else if (type == CommandType.RESPONSE.getValue()) {
            if (byteBuf.readableBytes() < RpcProtocolV1.MIN_RESPONSE_LENGTH - 4) {
                byteBuf.resetReaderIndex();
                return;
            }

            CommandCode commandCode = CommandCode.valueOf(byteBuf.readShort());

            ResponseCommand command;

            if (commandCode == CommandCode.HEARTBEAT) {
                command = new HeartbeatAckCommand(protocolCode);
            } else {
                command = new ResponseCommand(protocolCode);
            }

            command.setCommandCode(commandCode);
            command.setId(byteBuf.readInt());
            command.setSerializer(byteBuf.readByte());
            command.setProtocolSwitch(ProtocolSwitch.parse(byteBuf.readByte()));
            command.setStatus(RpcStatus.valueOf(byteBuf.readShort()));

            short clazzLength = byteBuf.readShort();
            int contentLength = byteBuf.readInt();

            int requiredLength = clazzLength + contentLength;

            boolean hasCrc32 = command.getProtocolSwitch().isOn(0);
            if (hasCrc32) {
                // CRC32
                requiredLength += 4;
            }

            if (byteBuf.readableBytes() >= requiredLength) {
                if (clazzLength > 0) {
                    byte[] clazz = new byte[clazzLength];
                    byteBuf.readBytes(clazz);
                    command.setClazz(clazz);
                } else {
                    command.setClazz(new byte[0]);
                }

                if (contentLength > 0) {
                    byte[] content = new byte[contentLength];
                    byteBuf.readBytes(content);
                    command.setContent(content);
                } else {
                    command.setContent(new byte[0]);
                }

                if (hasCrc32) {
                    checkCrc32(byteBuf, startIndex);
                }

                out.add(command);
            } else {
                byteBuf.resetReaderIndex();
            }
        } else {
            throw new ProtocolException("Unknown command type : " + type);
        }
    }

    private void checkCrc32(ByteBuf byteBuf, int startIndex) {
        int endIndex = byteBuf.readerIndex();
        byte[] msg = new byte[endIndex - startIndex];
        byteBuf.getBytes(startIndex, msg, 0, msg.length);

        int exceptedCrc32 = byteBuf.readInt();
        if (Crc32Util.calculate(msg) != exceptedCrc32) {
            throw new ProtocolException("Invalid CRC32!");
        }
    }
}
