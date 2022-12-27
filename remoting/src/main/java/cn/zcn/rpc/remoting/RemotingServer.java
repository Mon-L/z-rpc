package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.config.Option;
import cn.zcn.rpc.remoting.config.ServerOptions;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import cn.zcn.rpc.remoting.protocol.MessageDecoder;
import cn.zcn.rpc.remoting.protocol.MessageEncoder;
import cn.zcn.rpc.remoting.utils.NamedThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.NettyRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class RemotingServer extends AbstractLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingServer.class);

    private final int port;
    private final String host;
    private final ServerOptions options = new ServerOptions();

    private final ProtocolManager protocolManager = new ProtocolManager();
    private final SerializerManager serializerManager = new SerializerManager();

    private RpcInboundHandler rpcInboundHandler;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    private final RequestProcessor requestProcessor = new RequestProcessor(options);

    public RemotingServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    protected void doStart() throws LifecycleException {
        try {
            LOGGER.warn("Prepare to start remoting server.");
            if (doStartup()) {
                LOGGER.warn("Remoting server has started on port {}.", port);
            } else {
                LOGGER.warn("Failed start remoting server on port {}.", port);
                throw new LifecycleException("Failed start remoting server on port " + port);
            }
        } catch (Throwable t) {
            stop();
            throw new LifecycleException(t, "Failed to start remoting server!");
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
            workerGroup = new EpollEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2,
                    new NamedThreadFactory("netty-server-worker-group"));
        } else {
            channelClass = NioServerSocketChannel.class;
            bossGroup = new NioEventLoopGroup(1, new NamedThreadFactory("netty-boss-group"));
            workerGroup = new NioEventLoopGroup(NettyRuntime.availableProcessors() * 2,
                    new NamedThreadFactory("netty-server-worker-group"));
        }

        this.requestProcessor.start();

        this.rpcInboundHandler = new RpcInboundHandler(options, protocolManager, serializerManager, requestProcessor);
        ConnectionEventHandler connectionEventHandler = new ConnectionEventHandler();

        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(channelClass)
                .option(ChannelOption.SO_BACKLOG, options.getOption(ServerOptions.SO_BACKLOG))
                .option(ChannelOption.SO_REUSEADDR, options.getOption(ServerOptions.SO_REUSEADDR))
                .childOption(ChannelOption.TCP_NODELAY, options.getOption(ServerOptions.TCP_NODELAY))
                .childOption(ChannelOption.SO_KEEPALIVE, options.getOption(ServerOptions.SO_KEEPALIVE))
                .childOption(ChannelOption.SO_SNDBUF, options.getOption(ServerOptions.SO_SNDBUF))
                .childOption(ChannelOption.SO_RCVBUF, options.getOption(ServerOptions.SO_RCVBUF))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new MessageDecoder(protocolManager));
                        pipeline.addLast(new MessageEncoder(protocolManager));
                        pipeline.addLast(connectionEventHandler);
                        pipeline.addLast(rpcInboundHandler);
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

        if (this.requestProcessor != null) {
            this.requestProcessor.stop();
        }

        return true;
    }

    public void registerRequestHandler(RequestHandler<?> handler) {
        this.requestProcessor.registerRequestHandler(handler);
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public SerializerManager getSerializerManager() {
        return serializerManager;
    }

    public <T> void option(Option<T> option, T value) {
        options.setOption(option, value);
    }
}
