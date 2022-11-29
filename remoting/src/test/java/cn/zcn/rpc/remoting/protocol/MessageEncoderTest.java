package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.Protocol;
import cn.zcn.rpc.remoting.ProtocolEncoder;
import cn.zcn.rpc.remoting.ProtocolManager;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.EncoderException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MessageEncoderTest {

    private ProtocolManager protocolManager;
    private MessageEncoder messageEncoder;
    private EmbeddedChannel channel;

    @Before
    public void before() {
        protocolManager = Mockito.mock(ProtocolManager.class);
        Protocol protocol = Mockito.mock(Protocol.class);

        ProtocolEncoder protocolEncoder = Mockito.mock(ProtocolEncoder.class);
        Mockito.when(protocol.getEncoder()).thenReturn(protocolEncoder);

        messageEncoder = Mockito.spy(new MessageEncoder(protocolManager));
        channel = new EmbeddedChannel(messageEncoder);
    }

    @Test
    public void testEncodeWhenUnknownProtocol() {
        Assertions.assertThatExceptionOfType(EncoderException.class).isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                channel.writeOutbound("foo");
            }
        });
    }

    @Test
    public void testEncodeWhenUnmatchedProtocol() {
        channel.attr(Protocol.PROTOCOL).set(ProtocolCode.from((byte) 100, (byte) 100));
        Assertions.assertThatExceptionOfType(EncoderException.class).isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                channel.writeOutbound("foo");
            }
        });
    }
}
