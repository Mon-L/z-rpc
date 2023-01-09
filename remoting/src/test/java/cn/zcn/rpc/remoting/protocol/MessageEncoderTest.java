package cn.zcn.rpc.remoting.protocol;

import static org.assertj.core.api.Assertions.*;

import cn.zcn.rpc.remoting.Protocol;
import cn.zcn.rpc.remoting.ProtocolEncoder;
import cn.zcn.rpc.remoting.constants.AttributeKeys;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.EncoderException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MessageEncoderTest {
    private MessageEncoder messageEncoder;
    private EmbeddedChannel channel;

    @Before
    public void before() {
        Protocol protocol = Mockito.mock(Protocol.class);

        ProtocolEncoder protocolEncoder = Mockito.mock(ProtocolEncoder.class);
        Mockito.when(protocol.getEncoder()).thenReturn(protocolEncoder);

        messageEncoder = Mockito.spy(new MessageEncoder());
        channel = new EmbeddedChannel(messageEncoder);
    }

    @Test
    public void testEncodeWhenUnknownProtocol() {
        assertThatExceptionOfType(EncoderException.class).isThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() throws Throwable {
                channel.writeOutbound("foo");
            }
        });
    }

    @Test
    public void testEncodeWhenUnmatchedProtocol() {
        channel.attr(AttributeKeys.PROTOCOL).set(ProtocolCode.from((byte) 100, (byte) 100));
        assertThatExceptionOfType(EncoderException.class).isThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() throws Throwable {
                channel.writeOutbound("foo");
            }
        });
    }
}
