package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.config.Options;
import cn.zcn.rpc.remoting.config.RpcOptions;
import cn.zcn.rpc.remoting.config.ServerOptions;
import cn.zcn.rpc.remoting.exception.ServiceException;
import cn.zcn.rpc.remoting.protocol.*;
import cn.zcn.rpc.remoting.serialization.Serializer;
import cn.zcn.rpc.remoting.utils.NetUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 请求调用上下文
 *
 * @author zicung
 */
public class DefaultInvocationContext implements InvocationContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInvocationContext.class);

    private final Channel channel;
    private final RequestCommand request;

    private Protocol protocol;
    private Serializer serializer;
    private int timeout;
    private long startTimeMillis;
    private long readyTimeMillis;

    public DefaultInvocationContext(Channel channel, RequestCommand request) {
        this.request = request;
        this.channel = channel;
    }

    @Override
    public int getRequestId() {
        return request.getId();
    }

    @Override
    public String getRemoteHost() {
        return NetUtil.getRemoteHost(channel);
    }

    @Override
    public int getRemotePort() {
        return NetUtil.getRemotePort(channel);
    }

    @Override
    public long getReadyTimeMillis() {
        return readyTimeMillis;
    }

    public void setReadyTimeMillis(long millis) {
        this.readyTimeMillis = millis;
    }

    @Override
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long millis) {
        this.startTimeMillis = millis;
    }

    @Override
    public boolean isTimeout() {
        if (timeout <= 0 || request.getCommandType() == CommandType.REQUEST_ONEWAY) {
            return false;
        }

        return System.currentTimeMillis() - readyTimeMillis > timeout;
    }

    @Override
    public int getRemainingTime() {
        long elapsed = System.currentTimeMillis() - readyTimeMillis;
        if (elapsed > timeout) {
            return 0;
        }

        return (int) (timeout - elapsed);
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeout = timeoutMillis;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void writeAndFlushResponse(Object obj) {
        writeAndFlushResponse(obj, RpcStatus.OK);
    }

    @Override
    public void writeAndFlushException(Throwable throwable) {
        ServiceException serviceException = new ServiceException(throwable.getMessage());
        serviceException.setStackTrace(throwable.getStackTrace());
        writeAndFlushResponse(serviceException, RpcStatus.SERVICE_ERROR);
    }

    @Override
    public void writeAndFlushResponse(Object obj, RpcStatus status) {
        if (obj == null || request.getCommandType() == CommandType.REQUEST_ONEWAY) {
            return;
        }

        if (status == null) {
            throw new IllegalArgumentException("RpcStatus should not be null.");
        }

        if (!isTimeout()) {
            Options options = channel.attr(RpcOptions.OPTIONS_ATTRIBUTE_KEY).get();

            CommandFactory commandFactory = protocol.getCommandFactory();
            BaseCommand response = commandFactory.createResponseCommand(request, status);

            response.setClazz(obj.getClass().getName().getBytes(options.getOption(ServerOptions.CHARSET)));

            try {
                byte[] content = serializer.serialize(obj);
                response.setContent(content);
            } catch (Throwable t) {
                response = commandFactory.createResponseCommand(request, RpcStatus.SERIALIZATION_ERROR);
            }

            channel.writeAndFlush(response).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    LOGGER.error("Failed to send response. Request id:{}, To:{}", request.getId(), NetUtil.getRemoteAddress(channel));
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Sent response. Request id:{},  To:{}", request.getId(), NetUtil.getRemoteAddress(channel));
                    }
                }
            });
        }
    }
}