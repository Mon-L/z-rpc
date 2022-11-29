package cn.zcn.rpc.remoting.connection;

import cn.zcn.rpc.remoting.DefaultInvokePromise;
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

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SingleConnectionGroupTest extends AbstractEventLoopGroupTest {

    private Url url;
    private ServerBootstrap server;
    private Bootstrap client;

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
        assertTrue(future.awaitUninterruptibly(2000));
        assertTrue(future.isSuccess());
        assertTrue(future.getNow().isActive());

        //acquire connection
        Future<Connection> future2 = connectionGroup.acquireConnection();
        assertTrue(future2.awaitUninterruptibly(1000));
        assertTrue(future2.isSuccess());
        assertTrue(future2.getNow().isActive());

        //connection must be same in single connection group
        assertSame(future.getNow(), future2.getNow());

        //release connection
        Future<Void> releaseFuture = connectionGroup.releaseConnection(future.getNow());
        assertTrue(releaseFuture.awaitUninterruptibly(1000));
        assertTrue(releaseFuture.isSuccess());

        sc.close().syncUninterruptibly();
        connectionGroup.close();
    }

    @Test
    public void testAcquireConnectionFromClosedGroup() {
        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);
        connectionGroup.close();

        Future<Connection> future = connectionGroup.acquireConnection();
        assertTrue(future.awaitUninterruptibly(2000));
        assertFalse(future.isSuccess());
        assertInstanceOf(IllegalStateException.class, future.cause());
    }

    @Test
    public void testReleaseConnection() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);

        //acquire connection
        Future<Connection> future = connectionGroup.acquireConnection();
        assertTrue(future.awaitUninterruptibly(2000));
        assertTrue(future.isSuccess());
        assertTrue(future.getNow().isActive());

        //release
        Future<Void> releaseFuture = connectionGroup.releaseConnection(future.getNow());
        assertTrue(releaseFuture.awaitUninterruptibly(2000));
        assertTrue(releaseFuture.isSuccess());

        //test release connection with error group key
        Connection conn = future.getNow();

        //check connection state
        assertTrue(conn.isActive());

        //modify connection group key
        conn.getChannel()
                .attr(Connection.CONNECTION_GROUP_KEY)
                .set(new Url.Builder(new LocalAddress(TestUtils.getLocalAddressId())).build());

        //release connection
        releaseFuture = connectionGroup.releaseConnection(conn);
        assertTrue(releaseFuture.awaitUninterruptibly(2000));
        assertFalse(releaseFuture.isSuccess());
        assertInstanceOf(IllegalArgumentException.class, releaseFuture.cause());

        //connection is closed
        assertFalse(conn.isActive());

        sc.close().syncUninterruptibly();
    }

    @Test
    public void testReleaseConnectionToClosedGroup() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);

        //acquire connection
        Future<Connection> future = connectionGroup.acquireConnection();
        assertTrue(future.awaitUninterruptibly(2000));
        assertTrue(future.isSuccess());
        assertTrue(future.getNow().isActive());

        //close group
        Future<Void> closeFuture = connectionGroup.close();
        assertTrue(closeFuture.awaitUninterruptibly(2000));

        //release connection
        Future<Void> releaseFuture = connectionGroup.releaseConnection(future.getNow());
        assertTrue(releaseFuture.awaitUninterruptibly(2000));
        assertFalse(releaseFuture.isSuccess());
        assertInstanceOf(IllegalStateException.class, releaseFuture.cause());

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
                    fail("Should not reach here!");
                }
            }).start();
        }

        //wait for all thread
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertEquals(num, futures.size());

        Set<Connection> set = new HashSet<>();
        for (Future<Connection> f : futures) {
            assertTrue(f.awaitUninterruptibly(2000));
            assertTrue(f.isSuccess());
            set.add(f.getNow());
        }

        //all connection must be same
        assertEquals(1, set.size());

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
        assertTrue(future.awaitUninterruptibly(3000));
        assertTrue(future.isSuccess());

        assertEquals(acquiredMillis, connectionGroup.getLastAcquiredTime(), 3);

        sc.close().syncUninterruptibly();
        connectionGroup.close();
    }

    @Test
    public void testCanClose() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);
        assertTrue(connectionGroup.canClose());

        Future<Connection> future = connectionGroup.acquireConnection();
        assertTrue(future.awaitUninterruptibly(2000));

        Connection connection = future.getNow();

        //存在一条连接，但是没有完成的请求
        assertTrue(connectionGroup.canClose());

        //存在一条连接， 还有未完成的请求
        connection.addPromise(1, new DefaultInvokePromise(this.client.config().group().next().newPromise()));
        assertFalse(connectionGroup.canClose());

        assertTrue(connection.close().awaitUninterruptibly(2000));
        assertTrue(connectionGroup.canClose());

        sc.close().syncUninterruptibly();
        connectionGroup.close();
    }

    @Test
    public void testClose() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();

        ConnectionGroup connectionGroup = new SingleConnectionGroup(url, client);
        Future<Connection> future = connectionGroup.acquireConnection();
        assertTrue(future.awaitUninterruptibly(2000));

        assertEquals(1, connectionGroup.getActiveCount());

        assertTrue(connectionGroup.close().awaitUninterruptibly(2000));
        assertEquals(0, connectionGroup.getActiveCount());

        sc.close().syncUninterruptibly();
    }
}