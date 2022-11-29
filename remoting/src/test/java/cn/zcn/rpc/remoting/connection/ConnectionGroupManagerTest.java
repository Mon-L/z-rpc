package cn.zcn.rpc.remoting.connection;

import cn.zcn.rpc.remoting.Url;
import cn.zcn.rpc.remoting.test.TestUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionGroupManagerTest extends AbstractEventLoopGroupTest {

    private Url url;
    private ServerBootstrap server;
    private Bootstrap bootstrap;

    @BeforeEach
    public void before() {
        this.url = new Url.Builder(new LocalAddress(TestUtils.getLocalAddressId())).build();

        this.server = new ServerBootstrap()
                .channel(LocalServerChannel.class)
                .group(eventLoopGroup)
                .childHandler(new ChannelInitializer<LocalChannel>() {
                    @Override
                    protected void initChannel(LocalChannel channel) {
                        channel.pipeline().addLast(new ChannelInboundHandlerAdapter());
                    }
                });

        this.bootstrap = new Bootstrap()
                .channel(LocalChannel.class)
                .group(eventLoopGroup)
                .handler(new ChannelInitializer<LocalChannel>() {
                    @Override
                    protected void initChannel(LocalChannel channel) {
                        channel.pipeline().addLast(new ChannelInboundHandlerAdapter());
                    }
                });
    }

    @Test
    public void testStartup() {
        ConnectionGroupManager manager = new ConnectionGroupManager(bootstrap);

        //throw exception before startup
        assertThrows(IllegalStateException.class, () -> manager.getConnectionGroup(url));

        manager.start();

        assertNotNull(manager.getConnectionGroup(url));

        manager.stop();
    }

    @Test
    public void testGetConnectionGroup() throws InterruptedException {
        ConnectionGroupManager manager = new ConnectionGroupManager(bootstrap);
        manager.start();

        Set<ConnectionGroup> groups = Collections.synchronizedSet(new HashSet<>());
        CyclicBarrier barrier = new CyclicBarrier(10);
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    barrier.await();
                    groups.add(manager.getConnectionGroup(url));
                    latch.countDown();
                } catch (Exception e) {
                    fail(e);
                }
            }).start();
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(1, groups.size());

        manager.stop();
    }

    @Test
    public void testShutdown() throws InterruptedException {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroupManager manager = new ConnectionGroupManager(bootstrap);
        manager.start();

        //acquire connection
        ConnectionGroup group = manager.getConnectionGroup(url);
        Future<Connection> conn = group.acquireConnection();
        assertTrue(conn.awaitUninterruptibly(1500));
        assertTrue(conn.isSuccess());
        assertTrue(conn.getNow().isActive());

        manager.stop();

        //wait all collection groups be closed.
        TimeUnit.SECONDS.sleep(2);

        //check connection group state
        conn = group.acquireConnection();
        assertTrue(conn.awaitUninterruptibly(1500));
        assertInstanceOf(IllegalStateException.class, conn.cause());

        sc.close().awaitUninterruptibly();
    }
}
