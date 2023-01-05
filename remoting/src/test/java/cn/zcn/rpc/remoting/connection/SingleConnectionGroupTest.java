package cn.zcn.rpc.remoting.connection;

import cn.zcn.rpc.remoting.DefaultInvocationPromise;
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
import org.assertj.core.data.Offset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class SingleConnectionGroupTest extends AbstractEventLoopGroupTest {

    private Url url;
    private ServerBootstrap server;
    private Bootstrap client;

    @Before
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

        this.client = new Bootstrap()
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
    public void testAcquireConnection() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);

        //acquire connection
        Future<Connection> future = connectionGroup.acquireConnection();
        assertThat(future.awaitUninterruptibly(2000)).isTrue();
        assertThat(future.isSuccess()).isTrue();
        assertThat(future.getNow().isActive()).isTrue();

        //acquire connection
        Future<Connection> future2 = connectionGroup.acquireConnection();
        assertThat(future2.awaitUninterruptibly(1000)).isTrue();
        assertThat(future2.isSuccess()).isTrue();
        assertThat(future2.getNow().isActive()).isTrue();

        //connection must be same in single connection group
        assertThat(future2.getNow()).isSameAs(future.getNow());

        //release connection
        Future<Void> releaseFuture = connectionGroup.releaseConnection(future.getNow());
        assertThat(releaseFuture.awaitUninterruptibly(1000)).isTrue();
        assertThat(releaseFuture.isSuccess()).isTrue();

        sc.close().syncUninterruptibly();
        connectionGroup.close();
    }

    @Test
    public void testAcquireConnectionFromClosedGroup() {
        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);
        connectionGroup.close();

        Future<Connection> future = connectionGroup.acquireConnection();
        assertThat(future.awaitUninterruptibly(2000)).isTrue();
        assertThat(future.isSuccess()).isFalse();
        assertThat(future.cause()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testReleaseConnection() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);

        //acquire connection
        Future<Connection> future = connectionGroup.acquireConnection();
        assertThat(future.awaitUninterruptibly(2000)).isTrue();
        assertThat(future.isSuccess()).isTrue();
        assertThat(future.getNow().isActive()).isTrue();

        //release
        Future<Void> releaseFuture = connectionGroup.releaseConnection(future.getNow());
        assertThat(releaseFuture.awaitUninterruptibly(2000)).isTrue();
        assertThat(releaseFuture.isSuccess()).isTrue();

        //test release connection with error group key
        Connection conn = future.getNow();

        //check connection state
        assertThat(conn.isActive()).isTrue();

        //modify connection group key
        conn.getChannel()
                .attr(Connection.CONNECTION_GROUP_KEY)
                .set(new Url.Builder(new LocalAddress(TestUtils.getLocalAddressId())).build());

        //release connection
        releaseFuture = connectionGroup.releaseConnection(conn);
        assertThat(releaseFuture.awaitUninterruptibly(2000)).isTrue();
        assertThat(releaseFuture.isSuccess()).isFalse();
        assertThat(releaseFuture.cause()).isInstanceOf(IllegalArgumentException.class);

        //connection is closed
        assertThat(conn.isActive()).isFalse();

        sc.close().syncUninterruptibly();
    }

    @Test
    public void testReleaseConnectionToClosedGroup() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);

        //acquire connection
        Future<Connection> future = connectionGroup.acquireConnection();
        assertThat(future.awaitUninterruptibly(2000)).isTrue();
        assertThat(future.isSuccess()).isTrue();
        assertThat(future.getNow().isActive()).isTrue();

        //close group
        Future<Void> closeFuture = connectionGroup.close();
        assertThat(closeFuture.awaitUninterruptibly(2000)).isTrue();

        //release connection
        Future<Void> releaseFuture = connectionGroup.releaseConnection(future.getNow());
        assertThat(releaseFuture.awaitUninterruptibly(2000)).isTrue();
        assertThat(releaseFuture.isSuccess()).isFalse();
        assertThat(releaseFuture.cause()).isInstanceOf(IllegalStateException.class);

        sc.close().syncUninterruptibly();
    }

    @Test
    public void testConcurrentAcquireConnection() throws Exception {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();

        List<Future<Connection>> futures = Collections.synchronizedList(new LinkedList<>());
        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);

        //acquire connection
        int num = 20;
        CountDownLatch latch = new CountDownLatch(num);
        CyclicBarrier barrier = new CyclicBarrier(num);
        for (int i = 0; i < num; i++) {
            new Thread(() -> {
                try {
                    barrier.await();
                    futures.add(connectionGroup.acquireConnection());
                    latch.countDown();
                } catch (Exception e) {
                    Assert.fail("Should not reach here!");
                }
            }).start();
        }

        //wait for all thread
        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(futures.size()).isEqualTo(num);

        Set<Connection> set = new HashSet<>();
        for (Future<Connection> f : futures) {
            assertThat(f.awaitUninterruptibly(2000)).isTrue();
            assertThat(f.isSuccess()).isTrue();
            set.add(f.getNow());
        }

        //all connection must be same
        assertThat(set.size()).isEqualTo(1);

        sc.close().syncUninterruptibly();
        connectionGroup.close();
    }

    @Test
    public void testLastAcquiredTime() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();

        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);

        //acquire
        long acquiredMillis = System.currentTimeMillis();
        Future<Connection> future = connectionGroup.acquireConnection();
        assertThat(future.awaitUninterruptibly(3000)).isTrue();
        assertThat(future.isSuccess()).isTrue();

        assertThat(connectionGroup.getLastAcquiredTime()).isCloseTo(acquiredMillis, Offset.offset(3L));

        sc.close().syncUninterruptibly();
        connectionGroup.close();
    }

    @Test
    public void testCanClose() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);
        assertThat(connectionGroup.canClose()).isTrue();

        Future<Connection> future = connectionGroup.acquireConnection();
        assertThat(future.awaitUninterruptibly(2000)).isTrue();

        Connection connection = future.getNow();

        //存在一条连接，但是没有完成的请求
        assertThat(connectionGroup.canClose()).isTrue();

        //存在一条连接， 还有未完成的请求
        connection.addPromise(1, new DefaultInvocationPromise(this.client.config().group().next().newPromise()));
        assertThat(connectionGroup.canClose()).isFalse();

        assertThat(connection.close().awaitUninterruptibly(2000)).isTrue();
        assertThat(connectionGroup.canClose()).isTrue();

        sc.close().syncUninterruptibly();
        connectionGroup.close();
    }

    @Test
    public void testClose() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();

        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);
        Future<Connection> future = connectionGroup.acquireConnection();
        assertThat(future.awaitUninterruptibly(2000)).isTrue();

        assertThat(connectionGroup.getActiveCount()).isEqualTo(1);

        assertThat(connectionGroup.close().awaitUninterruptibly(2000)).isTrue();
        assertThat(connectionGroup.getActiveCount()).isEqualTo(0);

        sc.close().syncUninterruptibly();
    }
}