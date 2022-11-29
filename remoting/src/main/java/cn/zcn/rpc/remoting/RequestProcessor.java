package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.config.Options;
import cn.zcn.rpc.remoting.config.RpcOptions;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.exception.SerializationException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import cn.zcn.rpc.remoting.protocol.RequestCommand;
import cn.zcn.rpc.remoting.protocol.ResponseCommand;
import cn.zcn.rpc.remoting.protocol.RpcStatus;
import cn.zcn.rpc.remoting.serialization.Serializer;
import cn.zcn.rpc.remoting.utils.NamedThreadFactory;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

public class RequestProcessor extends AbstractLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestProcessor.class);

    private final Options options;
    private final Map<String, RequestHandler<?>> requestHandlers = new ConcurrentHashMap<>();

    private ExecutorService executor;

    RequestProcessor(Options options) {
        this.options = options;
    }

    @Override
    protected void doStart() throws LifecycleException {
        this.executor = new ThreadPoolExecutor(options.getOption(RpcOptions.PROCESSOR_CORE_SIZE),
                options.getOption(RpcOptions.PROCESSOR_MAX_SIZE),
                options.getOption(RpcOptions.PROCESSOR_KEEPALIVE),
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(options.getOption(RpcOptions.PROCESSOR_WORKER_QUEEN_SIZE)),
                new NamedThreadFactory("request-processor-executor"));
    }

    @Override
    protected void doStop() throws LifecycleException {
        if (executor != null) {
            this.executor.shutdown();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void execute(RpcContext rpcContext, RequestCommand command) {
        DefaultInvocationContext invocationContext = new DefaultInvocationContext(command, rpcContext);
        invocationContext.setReadyTimeMillis(System.currentTimeMillis());
        invocationContext.setTimeoutMillis(command.getTimeout());

        executor.execute(() -> {
            invocationContext.setStartTimeMillis(System.currentTimeMillis());

            Protocol protocol = rpcContext.getProtocol();
            Serializer serializer = rpcContext.getSerializer();
            try {
                //deserialize class
                String clazz = new String(command.getClazz(), options.getOption(RpcOptions.CHARSET));

                //deserialize content
                Object obj;
                try {
                    obj = serializer.deserialize(command.getContent(), clazz);
                } catch (SerializationException e) {
                    rpcContext.writeAndFlush(createResponseCommand(protocol, command, RpcStatus.DESERIALIZATION_ERROR));
                    return;
                }

                RequestHandler handler = requestHandlers.get(clazz);
                if (handler != null) {
                    if (handler.ignoredTimeoutRequest() && invocationContext.isTimeout()) {
                        //discard timeout request
                        LOGGER.warn("Request is discarded. request id[{}]. Remote host:{}, Timeout ms:{}, Total wait ms{}",
                                command.getId(), invocationContext.getRemoteHost(),
                                command.getTimeout(), System.currentTimeMillis() - invocationContext.getReadyTimeMillis());
                    } else {
                        handler.run(invocationContext, obj);
                    }
                } else {
                    LOGGER.debug("RequestHandler can not be found by {}. Request id:{}, Remote host:{}",
                            clazz, command.getId(), invocationContext.getRemoteHost());
                    rpcContext.writeAndFlush(createResponseCommand(protocol, command, RpcStatus.NO_REQUEST_PROCESSOR));
                }
            } catch (Throwable t) {
                rpcContext.writeAndFlush(createResponseCommand(protocol, command, RpcStatus.INTERNAL_SERVER_ERROR));
            }
        });
    }

    private ResponseCommand createResponseCommand(Protocol protocol, RequestCommand requestCommand, RpcStatus rpcStatus) {
        return protocol.getCommandFactory().createResponseCommand(requestCommand, rpcStatus);
    }

    public void registerRequestHandler(RequestHandler<?> handler) {
        if (isStarted()) {
            throw new IllegalStateException("RequestHandler was closed.");
        }

        if (handler == null) {
            throw new IllegalArgumentException("RequestHandler should not be null.");
        }

        String clazz = handler.acceptableClass();
        if (StringUtil.isNullOrEmpty(clazz)) {
            throw new IllegalArgumentException("RequestHandler should contains acceptable class.");
        }

        RequestHandler<?> oldHandler = requestHandlers.put(clazz, handler);
        if (oldHandler != null) {
            LOGGER.warn("Replace requestHandler, acceptable class :" + clazz);
        }
    }
}
