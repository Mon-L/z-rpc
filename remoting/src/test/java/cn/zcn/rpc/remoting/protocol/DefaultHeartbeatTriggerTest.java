package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.config.ClientOptions;
import cn.zcn.rpc.remoting.config.RpcOptions;
import cn.zcn.rpc.remoting.connection.Connection;
import cn.zcn.rpc.remoting.protocol.v1.RpcProtocolV1;
import cn.zcn.rpc.remoting.test.TestingChannelHandlerContext;
import cn.zcn.rpc.remoting.utils.IDGenerator;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.fail;

public class DefaultHeartbeatTriggerTest {

    private HeartbeatTrigger heartbeatTrigger;

    @BeforeEach
    public void beforeEach() {
        CommandFactory commandFactory = Mockito.mock(CommandFactory.class);

        HeartbeatCommand heartbeatCommand = new HeartbeatCommand(RpcProtocolV1.PROTOCOL_CODE);
        heartbeatCommand.setId(IDGenerator.getInstance().nextId());
        Mockito.when(commandFactory.createHeartbeatCommand()).thenReturn(heartbeatCommand);

        heartbeatTrigger = Mockito.spy(new DefaultHeartbeatTrigger(commandFactory));
    }

    @Test
    public void testHeartbeatFailureTooMany() {
        ClientOptions clientOptions = new ClientOptions();
        EmbeddedChannel channel = new EmbeddedChannel();
        Connection conn = new Connection(channel);

        channel.attr(Connection.CONNECTION_KEY).set(conn);
        channel.attr(RpcOptions.OPTIONS_ATTRIBUTE_KEY).set(clientOptions);

        TestingChannelHandlerContext ctx = new TestingChannelHandlerContext(channel);
        Assertions.assertTrue(conn.isActive());

        int maxFailures = clientOptions.getOption(ClientOptions.HEARTBEAT_MAX_FAILURES);

        for (int i = 0; i < maxFailures; i++) {
            heartbeatTrigger.heartbeatTriggered(ctx);

            try {
                TimeUnit.MILLISECONDS.sleep((long) DefaultHeartbeatTrigger.HEARTBEAT_TIMEOUT_MILLIS + 30);
            } catch (InterruptedException e) {
                fail("Should not reach here!", e);
            }

            Assertions.assertTrue(conn.isActive());
        }

        heartbeatTrigger.heartbeatTriggered(ctx);
        Assertions.assertFalse(conn.isActive());
    }
}
