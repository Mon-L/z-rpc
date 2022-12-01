package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.Protocol;
import cn.zcn.rpc.remoting.ProtocolDecoder;
import cn.zcn.rpc.remoting.ProtocolManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MessageDecoderTest {

    private ProtocolManager protocolManager;
    private MessageDecoder messageDecoder;
    private EmbeddedChannel channel;

    @Before
    public void before() {
        protocolManager = Mockito.mock(ProtocolManager.class);
        messageDecoder = Mockito.spy(new MessageDecoder(protocolManager));
        channel = new EmbeddedChannel(messageDecoder);
    }

    @Test
    public void testDecodeWhenUnknownProtocolCode() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(11);  //protocol code
        byteBuf.writeByte(11);  //protocol version
        byteBuf.writeByte(0);

        Assertions.assertThatExceptionOfType(DecoderException.class).isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                channel.writeInbound(byteBuf.retain());
            }
        });
    }

    @Test
    public void testDecodeWhenUnmatchedProtocolCode() {
        ProtocolCode protocolCode = ProtocolCode.from((byte) 1, (byte) 1);
        Protocol protocol = Mockito.mock(Protocol.class);
        Mockito.when(protocolManager.getProtocol(protocolCode)).thenReturn(protocol);

        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(1);  //protocol code
        byteBuf.writeByte(2);  //protocol version
        byteBuf.writeByte(0);

        Assertions.assertThatExceptionOfType(DecoderException.class).isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                channel.writeInbound(byteBuf.retain());
            }
        });
    }

    @Test
    public void testDecodeThenSuccessful() throws Exception {
        ProtocolCode protocolCode = ProtocolCode.from((byte) 1, (byte) 1);

        Protocol protocol = Mockito.mock(Protocol.class);
        ProtocolDecoder protocolDecoder = Mockito.mock(ProtocolDecoder.class);
        Mockito.when(protocol.getDecoder()).thenReturn(protocolDecoder);
        Mockito.when(protocolManager.getProtocol(protocolCode)).thenReturn(protocol);

        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(1);  //protocol code
        byteBuf.writeByte(1);  //protocol version
        byteBuf.writeByte(0);
        channel.writeInbound(byteBuf.retain());

        Mockito.verify(protocolDecoder, Mockito.times(1))
                .decode(Mockito.any(), Mockito.any(), Mockito.any());
    }
}
