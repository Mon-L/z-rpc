package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.Protocol;
import cn.zcn.rpc.remoting.ProtocolDecoder;
import cn.zcn.rpc.remoting.ProtocolManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class MessageDecoderTest {

    private EmbeddedChannel channel;

    @Before
    public void before() {
        MessageDecoder messageDecoder = Mockito.spy(new MessageDecoder());
        channel = new EmbeddedChannel(messageDecoder);
    }

    @Test
    public void testDecodeWhenUnknownProtocolCode() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(11);  //protocol code
        byteBuf.writeByte(11);  //protocol version
        byteBuf.writeByte(0);

        assertThatExceptionOfType(DecoderException.class).isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                channel.writeInbound(byteBuf.retain());
            }
        });
    }

    @Test
    public void testDecodeThenSuccessful() throws Exception {
        Protocol protocol = Mockito.mock(Protocol.class);
        Mockito.when(protocol.getProtocolCode()).thenReturn(ProtocolCode.from((byte) 11, (byte) 2));

        ProtocolManager.getInstance().registerProtocol(protocol.getProtocolCode(), protocol);

        ProtocolDecoder protocolDecoder = Mockito.mock(ProtocolDecoder.class);
        Mockito.when(protocol.getDecoder()).thenReturn(protocolDecoder);

        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(protocol.getProtocolCode().getCode());  //protocol code
        byteBuf.writeByte(protocol.getProtocolCode().getVersion());  //protocol version
        byteBuf.writeByte(0);
        channel.writeInbound(byteBuf.retain());

        Mockito.verify(protocol.getDecoder(), Mockito.times(1))
                .decode(Mockito.any(), Mockito.any(), Mockito.any());

        ProtocolManager.getInstance().unregisterProtocol(protocol.getProtocolCode());
    }
}
