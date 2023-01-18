package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.config.Option;
import cn.zcn.rpc.remoting.config.ServerOptions;
import cn.zcn.rpc.remoting.constants.AttributeKeys;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import cn.zcn.rpc.remoting.protocol.MessageDecoder;
import cn.zcn.rpc.remoting.protocol.MessageEncoder;
import cn.zcn.rpc.remoting.utils.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.NettyRuntime;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端
 *
 * @author zicung
 */
public class RemotingServer extends AbstractLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingServer.class);

    private final int port;
    private final String host;
    private final ServerOptions options = new ServerOptions();
    private final RequestCommandDispatcher requestCommandDispatcher = new RequestCommandDispatcher(options);

    private CommandInboundHandler commandInboundHandler;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    public RemotingServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    protected void doStart() throws LifecycleException {
        try {
            LOGGER.warn("Prepare to start remoting server.");
            if (doStartup()) {
                LOGGER.warn("Remoting server has started on {}:{}.", host, port);
            } else {
                LOGGER.warn("Failed start remoting server on port {}.", port);
                throw new LifecycleException("Failed start remoting server on port " + port);
            }
        } catch (Throwable t) {
            stop();
            throw new LifecycleException("Failed to start remoting server!", t);
        }
    }

    private boolean isEpollEnable() {
        return options.getOption(ServerOptions.NETTY_EPOLL) && Epoll.isAvailable();
    }

    private boolean doStartup() throws Exception {
        Class<? extends ServerSocketChannel> channelClass;
        if (isEpollEnable()) {
            channelClass = EpollServerSocketChannel.class;
            bossGroup = new EpollEventLoopGroup(1, new NamedThreadFactory("netty-boss-group"));
            workerGroup = new EpollEventLoopGroup(
                Runtime.getRuntime().availableProcessors() * 2,
                new NamedThreadFactory("netty-server-worker-group"));
        } else {
            channelClass = NioServerSocketChannel.class;
            bossGroup = new NioEventLoopGroup(1, new NamedThreadFactory("netty-boss-group"));
            workerGroup = new NioEventLoopGroup(
                NettyRuntime.availableProcessors() * 2, new NamedThreadFactory("netty-server-worker-group"));
        }

        this.requestCommandDispatcher.start();

        this.commandInboundHandler = new CommandInboundHandler(requestCommandDispatcher);
        ConnectionEventHandler connectionEventHandler = new ConnectionEventHandler();

        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
            .channel(channelClass)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .option(ChannelOption.SO_BACKLOG, options.getOption(ServerOptions.SO_BACKLOG))
            .option(ChannelOption.SO_REUSEADDR, options.getOption(ServerOptions.SO_REUSEADDR))
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .childOption(ChannelOption.TCP_NODELAY, options.getOption(ServerOptions.TCP_NODELAY))
            .childOption(ChannelOption.SO_KEEPALIVE, options.getOption(ServerOptions.SO_KEEPALIVE))
            .childOption(ChannelOption.SO_SNDBUF, options.getOption(ServerOptions.SO_SNDBUF))
            .childOption(ChannelOption.SO_RCVBUF, options.getOption(ServerOptions.SO_RCVBUF))
            .childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    socketChannel.attr(AttributeKeys.OPTIONS).set(options);

                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new MessageDecoder());
                    pipeline.addLast(new MessageEncoder());
                    pipeline.addLast(connectionEventHandler);
                    pipeline.addLast(commandInboundHandler);
                }
            });

        this.channelFuture = server.bind(new InetSocketAddress(host, port)).sync();
        return this.channelFuture.isSuccess();
    }

    @Override
    protected void doStop() {
        LOGGER.warn("Prepare to shutdown remoting server.");
        if (doShutdown()) {
            LOGGER.warn("Remoting server has stopped on port {}.", port);
        }
    }

    private boolean doShutdown() {
        if (this.channelFuture != null) {
            this.channelFuture.channel().close();
        }

        boolean syncShutdown = options.getOption(ServerOptions.SYNC_SHUTDOWN);
        if (this.bossGroup != null) {
            if (syncShutdown) {
                this.bossGroup.shutdownGracefully().awaitUninterruptibly();
            } else {
                this.bossGroup.shutdownGracefully();
            }
        }

        if (this.workerGroup != null) {
            if (syncShutdown) {
                this.workerGroup.shutdownGracefully().awaitUninterruptibly();
            } else {
                this.workerGroup.shutdownGracefully();
            }
        }

        this.requestCommandDispatcher.stop();

        return true;
    }

    public void registerRequestHandler(RequestHandler<?> handler) {
        this.requestCommandDispatcher.registerRequestHandler(handler);
    }

    public <T> void option(Option<T> option, T value) {
        options.setOption(option, value);
    }
}
