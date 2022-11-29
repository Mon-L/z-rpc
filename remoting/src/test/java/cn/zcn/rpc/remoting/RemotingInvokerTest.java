package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.config.RpcOptions;
import cn.zcn.rpc.remoting.connection.AbstractEventLoopGroupTest;
import cn.zcn.rpc.remoting.connection.Connection;
import cn.zcn.rpc.remoting.exception.TransportException;
import cn.zcn.rpc.remoting.protocol.ProtocolCode;
import cn.zcn.rpc.remoting.protocol.RequestCommand;
import cn.zcn.rpc.remoting.protocol.ResponseCommand;
import cn.zcn.rpc.remoting.test.TestUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class RemotingInvokerTest extends AbstractEventLoopGroupTest {

    private Url url;
    private ServerBootstrap server;
    private RemotingInvoker remotingInvoker;
    private boolean sendResponse = true;

    @BeforeEach
    public void before() {
        this.url = new Url.Builder(new LocalAddress(TestUtils.getLocalAddressId())).build();

        this.server = new ServerBootstrap()
                .channel(LocalServerChannel.class)
                .group(eventLoopGroup)
                .childHandler(new ChannelInitializer<LocalChannel>() {
                    @Override
                    protected void initChannel(LocalChannel channel) {

                        channel.pipeline().addLast(new ChannelDuplexHandler() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf byteBuf = (ByteBuf) msg;

                                if (sendResponse) {
                                    //把请求ID写回去
                                    ctx.channel().writeAndFlush(byteBuf.readInt());
                                }
                            }

                            @Override
                            public void write(ChannelHandlerContext ctx, Object id, ChannelPromise promise) throws Exception {
                                if (id instanceof Integer) {
                                    //把请求ID转化城字节数组并写回客户端
                                    ByteBuf out = UnpooledByteBufAllocator.DEFAULT.heapBuffer();
                                    out.writeInt((int) id);
                                    ctx.write(out, promise);
                                }
                            }
                        });
                    }
                });

        Bootstrap bootstrap = new Bootstrap()
                .channel(LocalChannel.class)
                .group(eventLoopGroup)
                .handler(new ChannelInitializer<LocalChannel>() {
                    @Override
                    protected void initChannel(LocalChannel channel) {
                        channel.pipeline().addLast(new ChannelDuplexHandler() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf in = (ByteBuf) msg;
                                Integer id = in.readInt();
                                InvokePromise<ResponseCommand> promise = ctx.channel().attr(Connection.CONNECTION_KEY).get().removePromise(id);
                                if (promise != null) {
                                    promise.setSuccess(new ResponseCommand(ProtocolCode.from((byte) 1, (byte) 1)));
                                }
                            }

                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                if (msg instanceof RequestCommand) {
                                    //把请求ID转化成字节数组并发送到服务端
                                    ByteBuf out = UnpooledByteBufAllocator.DEFAULT.heapBuffer();
                                    out.writeInt(((RequestCommand) msg).getId());
                                    ctx.write(out, promise);
                                }
                            }
                        });
                    }
                });

        this.remotingInvoker = new RemotingInvoker(new RpcOptions(), new ProtocolManager(), new SerializerManager(), bootstrap);
    }

    @Test
    public void testOneWayInvoke() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        remotingInvoker.start();

        Future<Void> future = remotingInvoker.oneWayInvoke(url, new Object());

        try {
            assertTrue(future.await(1, TimeUnit.SECONDS));
        } catch (Throwable t) {
            fail("Should not reach here.", t);
        }

        remotingInvoker.stop();
        sc.close().awaitUninterruptibly();
    }

    @Test
    public void testOneWayInvokeThenConnectFailed() {
        remotingInvoker.start();

        Future<Void> future = remotingInvoker.oneWayInvoke(url, new Object());

        try {
            assertTrue(future.await(1, TimeUnit.SECONDS));
            assertInstanceOf(TransportException.class, future.cause());
            assertInstanceOf(ConnectException.class, future.cause().getCause());
        } catch (Throwable t) {
            fail("Should not reach here.", t);
        }

        remotingInvoker.stop();
    }

    @Test
    public void testInvoke() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        remotingInvoker.start();

        Future<Void> future = remotingInvoker.invoke(url, new Object(), 3000);

        try {
            assertTrue(future.await(3000, TimeUnit.MILLISECONDS));
        } catch (Throwable t) {
            fail("Should not reach here.", t);
        }

        remotingInvoker.stop();
        sc.close().awaitUninterruptibly();
    }

    @Test
    public void testInvokeThenConnectFailed() {
        remotingInvoker.start();

        Future<Void> future = remotingInvoker.invoke(url, new Object(), 2000);

        try {
            assertTrue(future.await(1, TimeUnit.SECONDS));
            assertInstanceOf(TransportException.class, future.cause());
            assertInstanceOf(ConnectException.class, future.cause().getCause());
        } catch (Throwable t) {
            fail("Should not reach here.", t);
        }

        remotingInvoker.stop();
    }

    @Test
    public void testInvokeThenTimeout() {
        sendResponse = false;

        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        remotingInvoker.start();

        Future<Void> future = remotingInvoker.invoke(url, new Object(), 3000);

        try {
            assertFalse(future.await(3000, TimeUnit.MILLISECONDS));
        } catch (Throwable t) {
            fail("Should not reach here.", t);
        }

        remotingInvoker.stop();
        sc.close().awaitUninterruptibly();
    }
}
