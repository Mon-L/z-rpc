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
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiConnectionGroupTest extends AbstractEventLoopGroupTest {

    private Url url;
    private ServerBootstrap server;
    private Bootstrap client;

    @Before
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
        assertThat(future.isSuccess()).isTrue();
        assertThat(future.getNow().isActive()).isTrue();

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
            assertThat(future.awaitUninterruptibly(2, TimeUnit.SECONDS)).isTrue();
            queue.offer(future.getNow());
        }

        //连接数已耗尽，无法获得连接
        Future<Connection> pendingFuture = connectionGroup.acquireConnection();
        assertThat(pendingFuture.awaitUninterruptibly(2, TimeUnit.SECONDS)).isFalse();

        //释放一条连接
        Connection conn = queue.poll();
        connectionGroup.releaseConnection(conn);

        // pendingFuture 获得连接
        assertThat(pendingFuture.awaitUninterruptibly(2, TimeUnit.SECONDS)).isTrue();
        assertThat(pendingFuture.isSuccess()).isTrue();
        assertThat(pendingFuture.getNow()).isSameAs(conn);

        sc.close().syncUninterruptibly();
        connectionGroup.close();
    }

    @Test
    public void testAcquireConnectionFromClosedGroup() {
        ConnectionGroup connectionGroup = new MultiConnectionGroup(url, client, 1, 3000);
        connectionGroup.close();

        Future<Connection> future = connectionGroup.acquireConnection();
        assertThat(future.awaitUninterruptibly(2000)).isTrue();
        assertThat(future.isSuccess()).isFalse();
        assertThat(future.cause()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testAcquireConnectionThenTimeout() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new MultiConnectionGroup(url, client, 1, 3000);

        Future<Connection> future = connectionGroup.acquireConnection();
        assertThat(future.awaitUninterruptibly(2, TimeUnit.SECONDS)).isTrue();

        //连接数已耗尽，无法获得连接直至超时
        Future<Connection> pendingFuture = connectionGroup.acquireConnection();
        assertThat(pendingFuture.awaitUninterruptibly(3500, TimeUnit.MILLISECONDS)).isTrue();
        assertThat(pendingFuture.isSuccess()).isFalse();
        assertThat(pendingFuture.cause()).isInstanceOf(TimeoutException.class);

        sc.close().syncUninterruptibly();
        connectionGroup.close();
    }

    @Test
    public void testReleaseConnectionToClosedGroup() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new MultiConnectionGroup(url, client, 1, 3000);

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
    public void testReleaseConnection() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new MultiConnectionGroup(url, client, 1, 3000);

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
        conn.getChannel().attr(Connection.CONNECTION_GROUP_KEY).set(new Url.Builder(new LocalAddress(TestUtils.getLocalAddressId())).build());

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
    public void testClose() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        ConnectionGroup connectionGroup = new MultiConnectionGroup(url, client, 3, 3000);

        //acquire connections
        List<Connection> connections = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            Future<Connection> af = connectionGroup.acquireConnection();
            assertThat(af.awaitUninterruptibly(2, TimeUnit.SECONDS)).isTrue();
            connections.add(af.getNow());
        }

        //release connections
        for (Connection conn : connections) {
            Future<Void> rf = connectionGroup.releaseConnection(conn);
            assertThat(rf.awaitUninterruptibly(2, TimeUnit.SECONDS)).isTrue();
        }

        //close connection group
        Future<Void> closeFuture = connectionGroup.close();
        assertThat(closeFuture.awaitUninterruptibly(2, TimeUnit.SECONDS)).isTrue();

        //all connection should be closed
        for (Connection conn : connections) {
            assertThat(conn.isActive()).isFalse();
        }

        sc.close().syncUninterruptibly();
    }
}
