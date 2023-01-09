package cn.zcn.rpc.remoting.connection;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class AbstractEventLoopGroupTest {
    protected static EventLoopGroup eventLoopGroup;

    @BeforeClass
    public static void beforeAll() {
        eventLoopGroup = new DefaultEventLoopGroup(1);
    }

    @AfterClass
    public static void afterAll() {
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
