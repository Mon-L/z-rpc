package cn.zcn.rpc.remoting;

import java.util.Map;
import java.util.concurrent.*;

import cn.zcn.rpc.remoting.exception.SerializationException;
import cn.zcn.rpc.remoting.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zcn.rpc.remoting.config.Options;
import cn.zcn.rpc.remoting.config.RpcOptions;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import cn.zcn.rpc.remoting.serialization.Serializer;
import cn.zcn.rpc.remoting.utils.NamedThreadFactory;
import io.netty.util.internal.StringUtil;

/**
 * {@link RequestCommand} 分发器，将 {@code RequestCommand} 分发给对应的 {@link RequestHandler} 进行处理。
 * 使用线程池执行 {@code RequestHandler}。
 *
 * @author zicung
 */
public class RequestCommandDispatcher extends AbstractLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestCommandDispatcher.class);

    private final Options options;
    private final Map<String, RequestHandler<?>> requestHandlers = new ConcurrentHashMap<>();

    private ExecutorService executor;

    RequestCommandDispatcher(Options options) {
        this.options = options;
    }

    @Override
    protected void doStart() throws LifecycleException {
        this.executor = new ThreadPoolExecutor(
            options.getOption(RpcOptions.PROCESSOR_CORE_SIZE),
            options.getOption(RpcOptions.PROCESSOR_MAX_SIZE),
            options.getOption(RpcOptions.PROCESSOR_KEEPALIVE),
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(options.getOption(RpcOptions.PROCESSOR_WORKER_QUEEN_SIZE)),
            new NamedThreadFactory("request-handle-executor"));
    }

    @Override
    protected void doStop() throws LifecycleException {
        if (executor != null) {
            this.executor.shutdown();
        }
    }

    public void dispatch(CommandContext commandContext, RequestCommand requestCommand) {
        DefaultInvocationContext invocationContext = new DefaultInvocationContext(commandContext.getChannelContext(),
            requestCommand);
        invocationContext.setReadyTimeMillis(System.currentTimeMillis());
        invocationContext.setTimeoutMillis(requestCommand.getTimeout());
        invocationContext.setCommandFactory(commandContext.getProtocol().getCommandFactory());

        Serializer serializer = SerializerManager.getSerializer(requestCommand.getSerializer());
        if (serializer != null) {
            invocationContext.setSerializer(serializer);
        } else {
            writeAndFlushWithRpcStatus(commandContext, requestCommand, RpcStatus.DESERIALIZATION_ERROR);
            return;
        }

        executor.execute(() -> {
            invocationContext.setStartTimeMillis(System.currentTimeMillis());
            doDispatch(commandContext, invocationContext, serializer, requestCommand);
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void doDispatch(CommandContext commandContext, InvocationContext invocationContext, Serializer serializer,
                            RequestCommand requestCommand) {
        try {
            // deserialize class
            String clazz = new String(requestCommand.getClazz(), options.getOption(RpcOptions.CHARSET));

            // deserialize content
            Object obj;
            try {
                obj = serializer.deserialize(requestCommand.getContent(), clazz);
            } catch (SerializationException e) {
                writeAndFlushWithRpcStatus(commandContext, requestCommand, RpcStatus.DESERIALIZATION_ERROR);
                return;
            }

            RequestHandler handler = requestHandlers.get(clazz);
            if (handler == null) {
                LOGGER.debug(
                    "RequestHandler can not be found by {}. Request id:{}, From:{}, Request class: {}",
                    clazz,
                    requestCommand.getId(),
                    invocationContext.getRemoteHost(),
                    clazz);

                writeAndFlushWithRpcStatus(commandContext, requestCommand, RpcStatus.NO_REQUEST_PROCESSOR);
                return;
            }

            if (invocationContext.isTimeout() && handler.ignoredTimeoutRequest()) {
                // discard timeout request
                LOGGER.warn(
                    "Request is discarded. Request id[{}]. From:{}, Request waiting time: {}ms, Request timeout time:{}ms",
                    requestCommand.getId(),
                    invocationContext.getRemoteHost(),
                    System.currentTimeMillis() - invocationContext.getReadyTimeMillis(),
                    requestCommand.getTimeout());
            } else {
                try {
                    handler.handle(invocationContext, obj);
                } catch (Throwable t) {
                    writeAndFlushWithRpcStatus(commandContext, requestCommand, RpcStatus.SERVICE_ERROR);
                }
            }
        } catch (Throwable t) {
            writeAndFlushWithRpcStatus(commandContext, requestCommand, RpcStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void writeAndFlushWithRpcStatus(CommandContext commandContext, RequestCommand req, RpcStatus rpcStatus) {
        if (req.getCommandType() != CommandType.REQUEST_ONEWAY) {
            ResponseCommand response = commandContext.getProtocol().getCommandFactory()
                .createResponseCommand(req, rpcStatus);

            if (response != null) {
                commandContext.writeAndFlush(response);
            }
        }
    }

    /**
     * 注册请求处理器
     *
     * @param requestHandler 请求处理器
     */
    void registerRequestHandler(RequestHandler<?> requestHandler) {
        if (isStarted()) {
            throw new IllegalStateException("RequestCommandDispatcher was closed.");
        }

        if (requestHandler == null) {
            throw new IllegalArgumentException("RequestHandler should not be null.");
        }

        String clazz = requestHandler.acceptableClass();
        if (StringUtil.isNullOrEmpty(clazz)) {
            throw new IllegalArgumentException("RequestHandler should contains acceptable class.");
        }

        RequestHandler<?> oldHandler = requestHandlers.put(clazz, requestHandler);
        if (oldHandler != null) {
            LOGGER.warn("Replace requestHandler, acceptable class :" + clazz);
        }
    }
}
