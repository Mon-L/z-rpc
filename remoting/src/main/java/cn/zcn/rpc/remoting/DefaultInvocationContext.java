package cn.zcn.rpc.remoting;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zcn.rpc.remoting.config.Options;
import cn.zcn.rpc.remoting.config.ServerOptions;
import cn.zcn.rpc.remoting.constants.AttributeKeys;
import cn.zcn.rpc.remoting.exception.ServiceException;
import cn.zcn.rpc.remoting.protocol.*;
import cn.zcn.rpc.remoting.serialization.Serializer;
import cn.zcn.rpc.remoting.utils.NetUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * 请求调用上下文
 *
 * @author zicung
 */
public class DefaultInvocationContext implements InvocationContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInvocationContext.class);

    private final RequestCommand request;
    private final ChannelHandlerContext channelContext;

    private int timeout = -1;
    private long startTimeMillis;
    private long readyTimeMillis;
    private Serializer serializer;
    private CommandFactory commandFactory;

    public DefaultInvocationContext(ChannelHandlerContext channelContext, RequestCommand request) {
        this.request = request;
        this.channelContext = channelContext;
    }

    @Override
    public int getRequestId() {
        return request.getId();
    }

    @Override
    public String getRemoteHost() {
        return NetUtil.getRemoteHost(channelContext.channel());
    }

    @Override
    public int getRemotePort() {
        return NetUtil.getRemotePort(channelContext.channel());
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
        if (timeout < 0 || request.getCommandType() == CommandType.REQUEST_ONEWAY) {
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

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @Override
    public void writeAndFlushResponse(Object obj) {
        writeAndFlushResponse(obj, RpcStatus.OK);
    }

    @Override
    public void writeAndFlushResponse(Object obj, RpcStatus status) {
        if (obj == null || request.getCommandType() == CommandType.REQUEST_ONEWAY || isTimeout()) {
            return;
        }

        if (status == null) {
            throw new IllegalArgumentException("RpcStatus should not be null.");
        }

        Channel channel = channelContext.channel();

        BaseCommand response = commandFactory.createResponseCommand(request, status);
        if (response == null) {
            return;
        }

        try {
            byte[] content = serializer.serialize(obj);
            response.setContent(content);
        } catch (Throwable t) {
            response = commandFactory.createResponseCommand(request, RpcStatus.SERIALIZATION_ERROR);
        }

        channelContext.writeAndFlush(response).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                LOGGER.error(
                    "Failed to send response. Request id:{}, To:{}",
                    request.getId(),
                    NetUtil.getRemoteAddress(channel));
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                        "Sent response. Request id:{},  To:{}",
                        request.getId(),
                        NetUtil.getRemoteAddress(channel));
                }
            }
        });
    }
}
