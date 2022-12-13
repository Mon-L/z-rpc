package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.config.Options;
import cn.zcn.rpc.remoting.config.RpcOptions;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.exception.SerializationException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import cn.zcn.rpc.remoting.protocol.CommandType;
import cn.zcn.rpc.remoting.protocol.RequestCommand;
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
    public void execute(RpcContext rpcContext, RequestCommand requestCommand) {
        DefaultInvocationContext invocationContext = new DefaultInvocationContext(requestCommand, rpcContext);
        invocationContext.setReadyTimeMillis(System.currentTimeMillis());
        invocationContext.setTimeoutMillis(requestCommand.getTimeout());

        executor.execute(() -> {
            invocationContext.setStartTimeMillis(System.currentTimeMillis());

            Serializer serializer = rpcContext.getSerializer();
            try {
                //deserialize class
                String clazz = new String(requestCommand.getClazz(), options.getOption(RpcOptions.CHARSET));

                //deserialize content
                Object obj;
                try {
                    obj = serializer.deserialize(requestCommand.getContent(), clazz);
                } catch (SerializationException e) {
                    writeAndFlushWithRpcStatus(rpcContext, requestCommand, RpcStatus.DESERIALIZATION_ERROR);
                    return;
                }

                RequestHandler handler = requestHandlers.get(clazz);
                if (handler != null) {
                    if (handler.ignoredTimeoutRequest() && invocationContext.isTimeout()) {
                        //discard timeout request
                        LOGGER.warn("Request is discarded. request id[{}]. Remote host:{}, Timeout ms:{}, Total wait ms{}",
                                requestCommand.getId(), invocationContext.getRemoteHost(),
                                requestCommand.getTimeout(), System.currentTimeMillis() - invocationContext.getReadyTimeMillis());
                    } else {
                        try {
                            handler.run(invocationContext, obj);
                        } catch (Throwable t) {
                            invocationContext.writeAndFlushException(t);
                        }
                    }
                } else {
                    LOGGER.debug("RequestHandler can not be found by {}. Request id:{}, Remote host:{}",
                            clazz, requestCommand.getId(), invocationContext.getRemoteHost());

                    writeAndFlushWithRpcStatus(rpcContext, requestCommand, RpcStatus.NO_REQUEST_PROCESSOR);
                }
            } catch (Throwable t) {
                writeAndFlushWithRpcStatus(rpcContext, requestCommand, RpcStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    private void writeAndFlushWithRpcStatus(RpcContext rpcContext, RequestCommand req, RpcStatus rpcStatus) {
        if (req.getCommandType() != CommandType.REQUEST_ONEWAY) {
            rpcContext.writeAndFlush(rpcContext.getProtocol().getCommandFactory().createResponseCommand(req, rpcStatus));
        }
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
