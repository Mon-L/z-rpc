package cn.zcn.rpc.remoting.protocol.v1;

import cn.zcn.rpc.remoting.exception.ProtocolException;
import cn.zcn.rpc.remoting.ProtocolDecoder;
import cn.zcn.rpc.remoting.protocol.*;
import cn.zcn.rpc.remoting.test.TestingChannelHandlerContext;
import cn.zcn.rpc.remoting.utils.CRC32Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RpcProtocolDecoderTest {

    private EmbeddedChannel channel;
    private ChannelHandlerContext context;
    private ProtocolDecoder protocolDecoder;

    @Before
    public void before() {
        this.channel = new EmbeddedChannel();
        this.context = new TestingChannelHandlerContext(channel);
        this.protocolDecoder = new RpcProtocolDecoder();
    }

    @Test
    public void testDecodeWithUnmatchedProtocolCode() {
        ByteBuf in = Unpooled.buffer();
        in.writeByte(100);
        in.writeByte(100);

        for (int i = 0; i < 30; i++) {
            in.writeByte(i);
        }

        Assertions.assertThatExceptionOfType(ProtocolException.class).isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                protocolDecoder.decode(context, in, new ArrayList<>());
            }
        }).withMessageContaining("Excepted protocol ");
    }

    @Test
    public void testDecodeWithUnknownCommand() {
        ByteBuf in = Unpooled.buffer();
        in.writeByte(RpcProtocolV1.PROTOCOL_CODE.getCode());
        in.writeByte(RpcProtocolV1.PROTOCOL_CODE.getVersion());
        in.writeByte(126);

        for (int i = 0; i < 30; i++) {
            in.writeByte(i);
        }

        Assertions.assertThatExceptionOfType(ProtocolException.class).isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                protocolDecoder.decode(context, in, new ArrayList<>());
            }
        }).withMessageContaining("Unknown command type ");
    }

    @Test
    public void testDecodeRequestThenSuccess() {
        ByteBuf in = Unpooled.buffer();
        in.writeByte(RpcProtocolV1.PROTOCOL_CODE.getCode());
        in.writeByte(RpcProtocolV1.PROTOCOL_CODE.getVersion());
        in.writeShort(CommandType.REQUEST.getValue());
        in.writeShort(CommandCode.REQUEST.getValue());
        in.writeInt(1000);  // id
        in.writeByte(6);    //serializer

        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(0);  // crc 32

        in.writeByte(protocolSwitch.toByte());
        in.writeInt(400);   // timeout

        testDecodeWithIncompleteMessage(in);

        byte[] clazz = new byte[20];
        Arrays.fill(clazz, (byte) 2);
        in.writeShort(clazz.length);

        byte[] content = new byte[30];
        Arrays.fill(content, (byte) 3);
        in.writeInt(content.length);

        in.writeBytes(clazz);
        in.writeBytes(content);

        testDecodeWithIncompleteMessage(in);

        byte[] msg = new byte[in.readableBytes()];
        in.getBytes(0, msg, 0, msg.length);
        in.writeInt(CRC32Util.calculate(msg));

        try {
            List<Object> out = new ArrayList<>();
            protocolDecoder.decode(context, in, out);

            Assertions.assertThat(out.size()).isEqualTo(1);
            RequestCommand command = (RequestCommand) out.get(0);

            Assertions.assertThat(command.getProtocolCode()).isEqualTo(RpcProtocolV1.PROTOCOL_CODE);
            Assertions.assertThat(command.getCommandType()).isEqualTo(CommandType.REQUEST);
            Assertions.assertThat(command.getCommandCode()).isEqualTo(CommandCode.REQUEST);
            Assertions.assertThat(command.getId()).isEqualTo(1000);
            Assertions.assertThat(command.getSerializer()).isEqualTo((byte) 6);
            Assertions.assertThat(command.getTimeout()).isEqualTo(400);
            Assertions.assertThat(command.getProtocolSwitch()).isEqualTo(protocolSwitch);
            Assertions.assertThat(command.getClazz()).isEqualTo(clazz);
            Assertions.assertThat(command.getContent()).isEqualTo(content);
        } catch (Exception e) {
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    public void testDecodeResponseThenSuccess() {
        ByteBuf in = Unpooled.buffer();
        in.writeByte(RpcProtocolV1.PROTOCOL_CODE.getCode());
        in.writeByte(RpcProtocolV1.PROTOCOL_CODE.getVersion());
        in.writeShort(CommandType.RESPONSE.getValue());
        in.writeShort(CommandCode.RESPONSE.getValue());
        in.writeInt(1000);  // id
        in.writeByte(6);    //serializer

        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(0);  // crc 32

        in.writeByte(protocolSwitch.toByte());
        in.writeShort(RpcStatus.OK.getValue());

        testDecodeWithIncompleteMessage(in);

        byte[] clazz = new byte[20];
        Arrays.fill(clazz, (byte) 2);
        in.writeShort(clazz.length);

        byte[] content = new byte[30];
        Arrays.fill(content, (byte) 3);
        in.writeInt(content.length);

        in.writeBytes(clazz);
        in.writeBytes(content);

        testDecodeWithIncompleteMessage(in);

        byte[] msg = new byte[in.readableBytes()];
        in.getBytes(0, msg, 0, msg.length);
        in.writeInt(CRC32Util.calculate(msg));

        try {
            List<Object> out = new ArrayList<>();
            protocolDecoder.decode(context, in, out);

            Assertions.assertThat(out.size()).isEqualTo(1);
            ResponseCommand command = (ResponseCommand) out.get(0);

            Assertions.assertThat(command.getProtocolCode()).isEqualTo(RpcProtocolV1.PROTOCOL_CODE);
            Assertions.assertThat(command.getCommandType()).isEqualTo(CommandType.RESPONSE);
            Assertions.assertThat(command.getCommandCode()).isEqualTo(CommandCode.RESPONSE);
            Assertions.assertThat(command.getId()).isEqualTo(1000);
            Assertions.assertThat(command.getSerializer()).isEqualTo((byte) 6);
            Assertions.assertThat(command.getProtocolSwitch()).isEqualTo(protocolSwitch);
            Assertions.assertThat(command.getStatus()).isEqualTo(RpcStatus.OK);
            Assertions.assertThat(command.getClazz()).isEqualTo(clazz);
            Assertions.assertThat(command.getContent()).isEqualTo(content);
        } catch (Exception e) {
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    public void testDecodeRequestWithUnmatchedCRC32() {
        ByteBuf in = Unpooled.buffer();
        in.writeByte(RpcProtocolV1.PROTOCOL_CODE.getCode());
        in.writeByte(RpcProtocolV1.PROTOCOL_CODE.getVersion());
        in.writeShort(CommandType.REQUEST.getValue());
        in.writeShort(CommandCode.REQUEST.getValue());
        in.writeInt(1000);  // id
        in.writeByte(6);    //serializer

        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(0);  // crc 32

        in.writeByte(protocolSwitch.toByte());
        in.writeInt(400);   // timeout

        testDecodeWithIncompleteMessage(in);

        byte[] contentClass = new byte[20];
        Arrays.fill(contentClass, (byte) 2);
        in.writeShort(contentClass.length);

        byte[] content = new byte[30];
        Arrays.fill(content, (byte) 3);
        in.writeInt(content.length);

        in.writeBytes(contentClass);
        in.writeBytes(content);

        testDecodeWithIncompleteMessage(in);

        byte[] msg = new byte[in.readableBytes()];
        in.getBytes(0, msg, 0, msg.length - 1);
        in.writeInt(CRC32Util.calculate(msg));

        Assertions.assertThatExceptionOfType(ProtocolException.class).isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                List<Object> out = new ArrayList<>();
                protocolDecoder.decode(context, in, out);
            }
        });
    }

    private void testDecodeWithIncompleteMessage(ByteBuf in) {
        int readableBytes = in.readableBytes();
        try {
            protocolDecoder.decode(context, in, new ArrayList<>());
        } catch (Exception e) {
            Assert.fail("Should not reach here!");
        }

        //should reset reader index
        Assertions.assertThat(in.readableBytes()).isEqualTo(readableBytes);
    }
}
