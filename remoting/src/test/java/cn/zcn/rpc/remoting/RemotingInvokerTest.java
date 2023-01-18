package cn.zcn.rpc.remoting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import cn.zcn.rpc.remoting.config.RpcOptions;
import cn.zcn.rpc.remoting.connection.AbstractEventLoopGroupTest;
import cn.zcn.rpc.remoting.connection.Connection;
import cn.zcn.rpc.remoting.constants.AttributeKeys;
import cn.zcn.rpc.remoting.exception.RemotingException;
import cn.zcn.rpc.remoting.exception.ServiceException;
import cn.zcn.rpc.remoting.exception.TransportException;
import cn.zcn.rpc.remoting.protocol.*;
import cn.zcn.rpc.remoting.serialization.Serializer;
import cn.zcn.rpc.remoting.test.TestUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.util.concurrent.Future;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class RemotingInvokerTest extends AbstractEventLoopGroupTest {
    private Url url;
    private ServerBootstrap server;
    private RemotingInvoker remotingInvoker;
    private boolean testTimeoutCase = false;
    private boolean testServiceExceptionCase = false;

    @Before
    public void before() {
        this.url = new Url.Builder(new LocalAddress(TestUtils.getLocalAddressId())).build();

        this.server = new ServerBootstrap()
            .channel(LocalServerChannel.class)
            .group(eventLoopGroup)
            .childHandler(new ChannelInitializer<LocalChannel>() {

                @Override
                protected void initChannel(LocalChannel channel) {
                    channel.pipeline().addLast(new MessageDecoder());
                    channel.pipeline().addLast(new MessageEncoder());
                    channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            if (testTimeoutCase) {
                                return;
                            }

                            RequestCommand req = (RequestCommand) msg;

                            ResponseCommand resp = new ResponseCommand(req.getProtocolCode());
                            resp.setCommandCode(CommandCode.RESPONSE);
                            resp.setCommandType(CommandType.RESPONSE);
                            resp.setId(req.getId());
                            resp.setSerializer(SerializerManager.DEFAULT_SERIALIZER);
                            resp.setProtocolSwitch(ProtocolSwitch.parse((byte) 0));
                            resp.setClazz(new byte[0]);

                            if (testServiceExceptionCase) {
                                resp.setStatus(RpcStatus.SERVICE_ERROR);
                            } else {
                                resp.setStatus(RpcStatus.OK);
                                resp.setContent(new byte[0]);
                            }

                            ctx.writeAndFlush(resp);
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
                    channel.pipeline().addLast(new MessageDecoder());
                    channel.pipeline().addLast(new MessageEncoder());
                    channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            ResponseCommand resp = (ResponseCommand) msg;

                            InvocationPromise<ResponseCommand> promise = ctx.channel()
                                .attr(AttributeKeys.CONNECTION)
                                .get()
                                .removePromise(resp.getId());
                            if (promise != null) {
                                promise.cancelTimeout();
                                promise.setSuccess(resp);
                            }
                        }
                    });
                }
            });

        this.remotingInvoker = new RemotingInvoker(new RpcOptions(), bootstrap);
    }

    @Test
    public void testOneWayInvoke() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        remotingInvoker.start();

        Future<Void> future = remotingInvoker.oneWayInvoke(url, new Object());

        try {
            assertThat(future.await(1, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException e) {
            fail("Should not reach here.", e);
        }

        remotingInvoker.stop();
        sc.close().awaitUninterruptibly();
    }

    @Test
    public void testOneWayInvokeThenConnectFailed() {
        remotingInvoker.start();

        Future<Void> future = remotingInvoker.oneWayInvoke(url, new Object());

        try {
            assertThat(future.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(future.cause()).isInstanceOf(TransportException.class);
            assertThat(future.cause().getCause()).isInstanceOf(ConnectException.class);
        } catch (InterruptedException e) {
            fail("Should not reach here.", e);
        }

        remotingInvoker.stop();
    }

    @Test
    public void testInvoke() {
        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        remotingInvoker.start();

        Future<Void> future = remotingInvoker.invoke(url, new Object(), 3000);

        try {
            assertThat(future.await(3000, TimeUnit.MILLISECONDS)).isTrue();
        } catch (InterruptedException e) {
            fail("Should not reach here.", e);
        }

        remotingInvoker.stop();
        sc.close().awaitUninterruptibly();
    }

    @Test
    public void testInvokeThenConnectFailed() {
        remotingInvoker.start();

        Future<Void> future = remotingInvoker.invoke(url, new Object(), 2000);

        try {
            assertThat(future.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(future.cause()).isInstanceOf(TransportException.class);
            assertThat(future.cause().getCause()).isInstanceOf(ConnectException.class);
        } catch (InterruptedException e) {
            fail("Should not reach here.", e);
        }

        remotingInvoker.stop();
    }

    @Test
    public void testInvokeThenTimeout() {
        testTimeoutCase = true;

        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        remotingInvoker.start();

        Future<Void> future = remotingInvoker.invoke(url, new Object(), 3000);

        try {
            assertThat(future.await(3000, TimeUnit.MILLISECONDS)).isFalse();
        } catch (InterruptedException e) {
            fail("Should not reach here.", e);
        }

        remotingInvoker.stop();
        sc.close().awaitUninterruptibly();
    }

    @Test
    public void testInvokeThrowServiceException() {
        testServiceExceptionCase = true;

        Channel sc = this.server.bind(url.getAddress()).syncUninterruptibly().channel();
        remotingInvoker.start();

        Future<Void> future = remotingInvoker.invoke(url, new Object(), 3000);

        try {
            assertThat(future.await(3000, TimeUnit.MILLISECONDS)).isTrue();
            assertThat(future.cause()).isInstanceOf(RemotingException.class);
        } catch (InterruptedException e) {
            fail("Should not reach here.", e);
        }

        remotingInvoker.stop();
        sc.close().awaitUninterruptibly();
    }
}
