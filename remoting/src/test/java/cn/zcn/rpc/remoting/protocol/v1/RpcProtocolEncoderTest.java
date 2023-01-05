package cn.zcn.rpc.remoting.protocol.v1;

import cn.zcn.rpc.remoting.ProtocolEncoder;
import cn.zcn.rpc.remoting.exception.ProtocolException;
import cn.zcn.rpc.remoting.protocol.*;
import cn.zcn.rpc.remoting.test.TestingChannelHandlerContext;
import cn.zcn.rpc.remoting.utils.Crc32Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

public class RpcProtocolEncoderTest {

    private EmbeddedChannel channel;
    private ChannelHandlerContext context;
    private ProtocolEncoder protocolEncoder;

    @Before
    public void before() {
        this.channel = new EmbeddedChannel();
        this.context = new TestingChannelHandlerContext(channel);
        this.protocolEncoder = new RpcProtocolEncoder();
    }

    @Test
    public void testEncodeWithInvalidMessage() {
        ByteBuf out = Unpooled.buffer();
        Object msg = new Object();

        assertThatExceptionOfType(ProtocolException.class).isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                protocolEncoder.encode(context, msg, out);
            }
        });

        assertThat(out.readableBytes()).isEqualTo(0);
        assertThat(out.readerIndex()).isEqualTo(0);
        assertThat(out.writerIndex()).isEqualTo(0);
    }

    @Test
    public void testEncodeWithRequestCommand() {
        ProtocolCode protocolCode = ProtocolCode.from((byte) 2, (byte) 3);
        RequestCommand req = new RequestCommand(protocolCode, CommandType.REQUEST);
        req.setCommandCode(CommandCode.REQUEST);
        req.setId(3);
        req.setSerializer((byte) 4);

        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(0);
        req.setProtocolSwitch(protocolSwitch);

        req.setTimeout(40000);

        byte[] clazz = new byte[10];
        Arrays.fill(clazz, (byte) 9);
        req.setClazz(clazz);

        byte[] content = new byte[30];
        Arrays.fill(content, (byte) 5);
        req.setContent(content);

        ByteBuf out = Unpooled.buffer();
        try {
            protocolEncoder.encode(context, req, out);
        } catch (Exception e) {
            fail("Should not reach here!", e);
        }

        byte[] bytes = new byte[out.readableBytes()];
        out.getBytes(0, bytes);

        assertThat(out.readByte()).isEqualTo(protocolCode.getCode());
        assertThat(out.readByte()).isEqualTo(protocolCode.getVersion());
        assertThat(out.readShort()).isEqualTo(req.getCommandType().getValue());
        assertThat(out.readShort()).isEqualTo(req.getCommandCode().getValue());
        assertThat(out.readInt()).isEqualTo(req.getId());
        assertThat(out.readByte()).isEqualTo(req.getSerializer());
        assertThat(out.readByte()).isEqualTo(protocolSwitch.toByte());
        assertThat(out.readInt()).isEqualTo(req.getTimeout());
        assertThat(out.readShort()).isEqualTo((short) clazz.length);
        assertThat(out.readInt()).isEqualTo(content.length);
        assertThat(getBytes(out, clazz.length)).isEqualTo(clazz);
        assertThat(getBytes(out, content.length)).isEqualTo(content);

        checkCRC32(bytes, out.readInt());
    }

    @Test
    public void testEncodeWithResponseCommand() {
        ProtocolCode protocolCode = ProtocolCode.from((byte) 2, (byte) 3);
        ResponseCommand req = new ResponseCommand(protocolCode);
        req.setCommandCode(CommandCode.RESPONSE);
        req.setId(9);
        req.setSerializer((byte) 6);

        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(0);
        req.setProtocolSwitch(protocolSwitch);
        req.setStatus(RpcStatus.OK);

        byte[] clazz = new byte[10];
        Arrays.fill(clazz, (byte) 9);
        req.setClazz(clazz);

        byte[] content = new byte[30];
        Arrays.fill(content, (byte) 5);
        req.setContent(content);

        ByteBuf out = Unpooled.buffer();
        try {
            protocolEncoder.encode(context, req, out);
        } catch (Exception e) {
            fail("Should not reach here!", e);
        }

        byte[] bytes = new byte[out.readableBytes()];
        out.getBytes(0, bytes);

        assertThat(out.readByte()).isEqualTo(protocolCode.getCode());
        assertThat(out.readByte()).isEqualTo(protocolCode.getVersion());
        assertThat(out.readShort()).isEqualTo(req.getCommandType().getValue());
        assertThat(out.readShort()).isEqualTo(req.getCommandCode().getValue());
        assertThat(out.readInt()).isEqualTo(req.getId());
        assertThat(out.readByte()).isEqualTo(req.getSerializer());
        assertThat(out.readByte()).isEqualTo(protocolSwitch.toByte());
        assertThat(out.readShort()).isEqualTo(req.getStatus().getValue());
        assertThat(out.readShort()).isEqualTo((short) clazz.length);
        assertThat(out.readInt()).isEqualTo(content.length);
        assertThat(getBytes(out, clazz.length)).isEqualTo(clazz);
        assertThat(getBytes(out, content.length)).isEqualTo(content);

        checkCRC32(bytes, out.readInt());
    }

    private void checkCRC32(byte[] bytes, int i) {
        byte[] bytesWithoutCRC32 = new byte[bytes.length - 4];
        System.arraycopy(bytes, 0, bytesWithoutCRC32, 0, bytesWithoutCRC32.length);
        assertThat(Crc32Util.calculate(bytesWithoutCRC32)).isEqualTo(i);
    }

    private byte[] getBytes(ByteBuf byteBuf, int length) {
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }
}
