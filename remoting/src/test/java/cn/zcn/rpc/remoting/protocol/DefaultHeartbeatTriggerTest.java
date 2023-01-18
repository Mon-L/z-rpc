package cn.zcn.rpc.remoting.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import cn.zcn.rpc.remoting.config.ClientOptions;
import cn.zcn.rpc.remoting.connection.Connection;
import cn.zcn.rpc.remoting.constants.AttributeKeys;
import cn.zcn.rpc.remoting.protocol.v1.RpcProtocolV1;
import cn.zcn.rpc.remoting.utils.CommandIdGenerator;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.concurrent.TimeUnit;
import io.netty.handler.timeout.IdleStateHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DefaultHeartbeatTriggerTest {
    private HeartbeatTrigger heartbeatTrigger;

    @Before
    public void beforeEach() {
        CommandFactory commandFactory = Mockito.mock(CommandFactory.class);

        Mockito.when(commandFactory.createHeartbeatCommand()).thenAnswer(new Answer<HeartbeatCommand>() {
            @Override
            public HeartbeatCommand answer(InvocationOnMock invocationOnMock) throws Throwable {
                HeartbeatCommand heartbeatCommand = new HeartbeatCommand(RpcProtocolV1.PROTOCOL_CODE);
                heartbeatCommand.setId(CommandIdGenerator.getInstance().nextId());
                return heartbeatCommand;
            }
        });

        heartbeatTrigger = Mockito.spy(new DefaultHeartbeatTrigger(commandFactory));
    }

    @Test
    public void testHeartbeatFailureTooMany() throws InterruptedException {
        int heartbeatMaxFailures = 4;
        int heartbeatInterval = 500;

        ClientOptions clientOptions = new ClientOptions();
        clientOptions.setOption(ClientOptions.HEARTBEAT_MAX_FAILURES, heartbeatMaxFailures);
        clientOptions.setOption(ClientOptions.HEARTBEAT_INTERVAL_MILLIS, heartbeatInterval);

        IdleStateHandler idleStateHandler = new IdleStateHandler(heartbeatInterval, 0L, 0L,
            TimeUnit.MILLISECONDS);

        ChannelHandler ideStateEventHandler = new ChannelInboundHandlerAdapter() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                heartbeatTrigger.heartbeatTriggered(ctx);
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(idleStateHandler, ideStateEventHandler);
        channel.attr(AttributeKeys.OPTIONS).set(clientOptions);

        Connection conn = new Connection(channel);
        channel.attr(AttributeKeys.CONNECTION).set(conn);

        assertThat(conn.isActive()).isTrue();

        for (int i = 0; i < heartbeatMaxFailures; i++) {
            TimeUnit.MILLISECONDS.sleep(heartbeatInterval);
            channel.runScheduledPendingTasks();
        }

        TimeUnit.MILLISECONDS.sleep(DefaultHeartbeatTrigger.HEARTBEAT_TIMEOUT_MILLIS * heartbeatMaxFailures + 100);
        channel.runScheduledPendingTasks();

        //心跳超时多次，连接被关闭
        assertThat(conn.isActive()).isFalse();
    }
}
