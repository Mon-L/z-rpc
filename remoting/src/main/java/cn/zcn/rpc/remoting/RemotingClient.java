package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.config.ClientOptions;
import cn.zcn.rpc.remoting.config.Option;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.exception.RemotingException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import cn.zcn.rpc.remoting.protocol.MessageDecoder;
import cn.zcn.rpc.remoting.protocol.MessageEncoder;
import cn.zcn.rpc.remoting.utils.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemotingClient extends AbstractLifecycle {

    private final static Logger LOGGER = LoggerFactory.getLogger(RemotingClient.class);

    private final ClientOptions options = new ClientOptions();
    private final ProtocolManager protocolManager = new ProtocolManager();
    private final SerializerManager serializerManager = new SerializerManager();

    private EventLoopGroup workerGroup;
    private RequestProcessor requestProcessor;
    private RpcInboundHandler rpcInboundHandler;
    private RemotingInvoker remotingInvoker;

    @Override
    protected void doStart() throws LifecycleException {
        try {
            LOGGER.warn("Prepare to start remoting client.");
            doStartup();
            LOGGER.warn("Remoting client has started.");
        } catch (Throwable t) {
            stop();
            throw new LifecycleException("Failed to start remoting client!", t);
        }
    }

    private void doStartup() {
        Class<? extends SocketChannel> channelClazz;
        if (isEpollEnable()) {
            channelClazz = EpollSocketChannel.class;
            this.workerGroup = new EpollEventLoopGroup(NettyRuntime.availableProcessors() + 1,
                    new NamedThreadFactory("netty-client-worker-group"));
        } else {
            channelClazz = NioSocketChannel.class;
            this.workerGroup = new NioEventLoopGroup(NettyRuntime.availableProcessors() + 1,
                    new NamedThreadFactory("netty-client-worker-group"));
        }

        this.requestProcessor = new RequestProcessor(options);
        this.requestProcessor.start();
        this.rpcInboundHandler = new RpcInboundHandler(options, protocolManager, serializerManager, requestProcessor);
        ConnectionEventHandler connectionEventHandler = new ConnectionEventHandler();

        Bootstrap bootstrap = new Bootstrap()
                .channel(channelClazz)
                .group(workerGroup)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, options.getOption(ClientOptions.CONNECT_TIMEOUT_MILLIS))
                .option(ChannelOption.TCP_NODELAY, options.getOption(ClientOptions.TCP_NODELAY))
                .option(ChannelOption.SO_REUSEADDR, options.getOption(ClientOptions.SO_REUSEADDR))
                .option(ChannelOption.SO_KEEPALIVE, options.getOption(ClientOptions.SO_KEEPALIVE))
                .option(ChannelOption.SO_SNDBUF, options.getOption(ClientOptions.SO_SNDBUF))
                .option(ChannelOption.SO_RCVBUF, options.getOption(ClientOptions.SO_RCVBUF))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new MessageEncoder(protocolManager));
                        pipeline.addLast(new MessageDecoder(protocolManager));
                        pipeline.addLast(connectionEventHandler);
                        pipeline.addLast(rpcInboundHandler);
                    }
                });

        this.remotingInvoker = new RemotingInvoker(options, protocolManager, serializerManager, bootstrap);
        this.remotingInvoker.start();
    }

    @Override
    protected void doStop() throws LifecycleException {
        LOGGER.warn("Prepare to stop remoting client.");

        this.remotingInvoker.stop();

        boolean syncShutdown = options.getOption(ClientOptions.SYNC_SHUTDOWN);
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

        LOGGER.warn("Remoting client has stopped.");
    }

    private boolean isEpollEnable() {
        return options.getOption(ClientOptions.NETTY_EPOLL) && Epoll.isAvailable();
    }

    private void checkState() {
        if (!isStarted()) {
            throw new IllegalStateException("Remoting client was closed.");
        }
    }

    public void oneWayInvoke(Url url, Object obj) throws IllegalStateException, RemotingException {
        checkState();
        remotingInvoker.oneWayInvoke(url, obj);
    }

    public <T> Future<T> invoke(Url url, Object obj, int timeoutMillis) throws IllegalStateException, RemotingException {
        checkState();
        return remotingInvoker.invoke(url, obj, timeoutMillis);
    }

    public <T> void option(Option<T> option, T value) {
        options.setOption(option, value);
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public SerializerManager getSerializerManager() {
        return serializerManager;
    }

    public void registerRequestHandler(RequestHandler<?> handler) {
        this.requestProcessor.registerRequestHandler(handler);
    }
}
