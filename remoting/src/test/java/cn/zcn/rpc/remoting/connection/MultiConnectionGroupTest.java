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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class MultiConnectionGroupTest extends AbstractEventLoopGroupTest {

    private Url url;
    private ServerBootstrap server;
    private Bootstrap client;

    @BeforeEach
    public void before() {
        this.url = new Url.Builder(new LocalAddress(TestUtils.getLocalAddressId())).build();
        this.server = new ServerBootstrap().channel(LocalServerChannel.class).group(eventLoopGroup).childHandler(new ChannelInitializer<LocalChannel>() {
            @Override
            protected void initChannel(LocalChannel channel) {
                channel.pipeline().addLast(new ChannelInboundHandlerAdapter());
            }
        });

        this.client = new Bootstrap().channel(LocalChannel.class).group(eventLoopGroup).handler(new ChannelInitializer<LocalChannel>() {
            @Override
            protected void initChannel(LocalChannel channel) {
                channel.pipeline().addLast(new ChannelInboundHandlerAdapter());
            }
        });
    }

    @Test
    public void testAcquireConnection() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new MultiConnectionGroup(url, client, 3, 10000);

        Future<Connection> future = connectionGroup.acquireConnection();
        future.awaitUninterruptibly(3, TimeUnit.SECONDS);
        Assertions.assertTrue(future.isSuccess());
        Assertions.assertTrue(future.getNow().isActive());

        sc.close().syncUninterruptibly();
        connectionGroup.close();
    }

    @Test
    public void testAcquireConnectionThenWait() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new MultiConnectionGroup(url, client, 3, 10000);

        Queue<Connection> queue = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            Future<Connection> future = connectionGroup.acquireConnection();
            Assertions.assertTrue(future.awaitUninterruptibly(2, TimeUnit.SECONDS));
            queue.offer(future.getNow());
        }

        //连接数已耗尽，无法获得连接
        Future<Connection> pendingFuture = connectionGroup.acquireConnection();
        Assertions.assertFalse(pendingFuture.awaitUninterruptibly(2, TimeUnit.SECONDS));

        //释放一条连接
        Connection conn = queue.poll();
        connectionGroup.releaseConnection(conn);

        // pendingFuture 获得连接
        Assertions.assertTrue(pendingFuture.awaitUninterruptibly(2, TimeUnit.SECONDS));
        Assertions.assertTrue(pendingFuture.isSuccess());
        Assertions.assertSame(conn, pendingFuture.getNow());

        sc.close().syncUninterruptibly();
        connectionGroup.close();
    }

    @Test
    public void testAcquireConnectionFromClosedGroup() {
        ConnectionGroup connectionGroup = new MultiConnectionGroup(url, client, 1, 3000);
        connectionGroup.close();

        Future<Connection> future = connectionGroup.acquireConnection();
        assertTrue(future.awaitUninterruptibly(2000));
        assertFalse(future.isSuccess());
        assertInstanceOf(IllegalStateException.class, future.cause());
    }

    @Test
    public void testAcquireConnectionThenTimeout() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new MultiConnectionGroup(url, client, 1, 3000);

        Future<Connection> future = connectionGroup.acquireConnection();
        Assertions.assertTrue(future.awaitUninterruptibly(2, TimeUnit.SECONDS));

        //连接数已耗尽，无法获得连接直至超时
        Future<Connection> pendingFuture = connectionGroup.acquireConnection();
        Assertions.assertTrue(pendingFuture.awaitUninterruptibly(3500, TimeUnit.MILLISECONDS));
        Assertions.assertFalse(pendingFuture.isSuccess());
        Assertions.assertInstanceOf(TimeoutException.class, pendingFuture.cause());

        sc.close().syncUninterruptibly();
        connectionGroup.close();
    }

    @Test
    public void testReleaseConnectionToClosedGroup() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new MultiConnectionGroup(url, client, 1, 3000);

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
    public void testReleaseConnection() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new MultiConnectionGroup(url, client, 1, 3000);

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
        conn.getChannel().attr(Connection.CONNECTION_GROUP_KEY).set(new Url.Builder(new LocalAddress(TestUtils.getLocalAddressId())).build());

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
    public void testClose() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new MultiConnectionGroup(url, client, 3, 3000);

        //acquire connections
        List<Connection> connections = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            Future<Connection> af = connectionGroup.acquireConnection();
            Assertions.assertTrue(af.awaitUninterruptibly(2, TimeUnit.SECONDS));
            connections.add(af.getNow());
        }

        //release connections
        for (Connection conn : connections) {
            Future<Void> rf = connectionGroup.releaseConnection(conn);
            Assertions.assertTrue(rf.awaitUninterruptibly(2, TimeUnit.SECONDS));
        }

        //close connection group
        Future<Void> closeFuture = connectionGroup.close();
        Assertions.assertTrue(closeFuture.awaitUninterruptibly(2, TimeUnit.SECONDS));

        //all connection should be closed
        for (Connection conn : connections) {
            Assertions.assertFalse(conn.isActive());
        }

        sc.close().syncUninterruptibly();
    }
}
