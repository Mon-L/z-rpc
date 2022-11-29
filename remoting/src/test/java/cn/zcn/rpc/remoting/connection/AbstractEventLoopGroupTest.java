package cn.zcn.rpc.remoting.connection;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class AbstractEventLoopGroupTest {

    protected static EventLoopGroup eventLoopGroup;

    @BeforeAll
    public static void beforeAll() {
        eventLoopGroup = new DefaultEventLoopGroup(1);
    }

    @AfterAll
    public static void afterAll() {
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
