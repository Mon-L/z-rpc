package cn.zcn.rpc.remoting.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import cn.zcn.rpc.remoting.config.ClientOptions;
import cn.zcn.rpc.remoting.config.RpcOptions;
import cn.zcn.rpc.remoting.connection.Connection;
import cn.zcn.rpc.remoting.constants.AttributeKeys;
import cn.zcn.rpc.remoting.protocol.v1.RpcProtocolV1;
import cn.zcn.rpc.remoting.test.TestingChannelHandlerContext;
import cn.zcn.rpc.remoting.utils.CommandIdGenerator;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultHeartbeatTriggerTest {
    private HeartbeatTrigger heartbeatTrigger;

    @Before
    public void beforeEach() {
        CommandFactory commandFactory = Mockito.mock(CommandFactory.class);

        HeartbeatCommand heartbeatCommand = new HeartbeatCommand(RpcProtocolV1.PROTOCOL_CODE);
        heartbeatCommand.setId(CommandIdGenerator.getInstance().nextId());
        Mockito.when(commandFactory.createHeartbeatCommand()).thenReturn(heartbeatCommand);

        heartbeatTrigger = Mockito.spy(new DefaultHeartbeatTrigger(commandFactory));
    }

    @Test
    public void testHeartbeatFailureTooMany() {
        ClientOptions clientOptions = new ClientOptions();
        EmbeddedChannel channel = new EmbeddedChannel();
        Connection conn = new Connection(channel);

        channel.attr(AttributeKeys.CONNECTION).set(conn);
        channel.attr(AttributeKeys.OPTIONS).set(clientOptions);

        TestingChannelHandlerContext ctx = new TestingChannelHandlerContext(channel);
        assertThat(conn.isActive()).isTrue();

        int maxFailures = clientOptions.getOption(ClientOptions.HEARTBEAT_MAX_FAILURES);

        for (int i = 0; i < maxFailures; i++) {
            heartbeatTrigger.heartbeatTriggered(ctx);

            try {
                TimeUnit.MILLISECONDS.sleep((long) DefaultHeartbeatTrigger.HEARTBEAT_TIMEOUT_MILLIS + 30);
            } catch (InterruptedException e) {
                fail("Should not reach here!", e);
            }

            assertThat(conn.isActive()).isTrue();
        }

        heartbeatTrigger.heartbeatTriggered(ctx);
        assertThat(conn.isActive()).isFalse();
    }
}
