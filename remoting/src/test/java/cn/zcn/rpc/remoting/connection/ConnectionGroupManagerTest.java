package cn.zcn.rpc.remoting.connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConnectionGroupManagerTest extends AbstractEventLoopGroupTest {
    private Url url;
    private ServerBootstrap server;
    private Bootstrap bootstrap;

    @Before
    public void beforeEach() {
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

        // throw exception before startup
        assertThatThrownBy(() -> manager.getConnectionGroup(url)).isInstanceOf(IllegalStateException.class);

        manager.start();

        assertThat(manager.getConnectionGroup(url)).isNotNull();

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
                            Assert.fail(e.getMessage());
                        }
                    })
                    .start();
        }

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(groups.size()).isEqualTo(1);

        manager.stop();
    }

    @Test
    public void testShutdown() throws InterruptedException {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroupManager manager = new ConnectionGroupManager(bootstrap);
        manager.start();

        // acquire connection
        ConnectionGroup group = manager.getConnectionGroup(url);
        Future<Connection> conn = group.acquireConnection();
        assertThat(conn.awaitUninterruptibly(1500)).isTrue();
        assertThat(conn.isSuccess()).isTrue();
        assertThat(conn.getNow().isActive()).isTrue();

        manager.stop();

        // wait all collection groups be closed.
        TimeUnit.SECONDS.sleep(2);

        // check connection group state
        conn = group.acquireConnection();
        assertThat(conn.awaitUninterruptibly(1500)).isTrue();
        assertThat(conn.cause()).isInstanceOf(IllegalStateException.class);

        sc.close().awaitUninterruptibly();
    }
}
