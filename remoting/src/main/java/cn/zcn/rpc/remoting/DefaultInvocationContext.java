package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.config.ServerOptions;
import cn.zcn.rpc.remoting.protocol.*;
import cn.zcn.rpc.remoting.utils.NetUtil;

public class DefaultInvocationContext implements InvocationContext {

    private final RequestCommand request;
    private final RpcContext ctx;

    private int timeout;
    private long startTimeMillis;
    private long readyTimeMillis;

    public DefaultInvocationContext(RequestCommand request, RpcContext rpcContext) {
        this.request = request;
        this.ctx = rpcContext;
    }

    @Override
    public int getRequestId() {
        return request.getId();
    }

    @Override
    public String getRemoteHost() {
        return NetUtil.getRemoteHost(ctx.getChannelContext().channel());
    }

    @Override
    public int getRemotePort() {
        return NetUtil.getRemotePort(ctx.getChannelContext().channel());
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

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeout = timeoutMillis;
    }

    @Override
    public void writeAndFlushSuccessfullyResponse(Object obj) {
        if (obj == null) {
            return;
        }

        writeAndFlushResponse(obj, RpcStatus.OK);
    }

    @Override
    public void writeAndFlushResponse(Object obj, RpcStatus status) {
        if (obj == null) {
            return;
        }

        if (status == null) {
            throw new IllegalArgumentException("RpcStatus should not be null.");
        }

        CommandFactory commandFactory = ctx.getProtocol().getCommandFactory();
        Command response = commandFactory.createResponseCommand(request, status);

        response.setClazz(obj.getClass().getName().getBytes(ctx.getOptions().getOption(ServerOptions.CHARSET)));

        try {
            byte[] content = ctx.getSerializer().serialize(obj);
            response.setContent(content);
        } catch (Throwable t) {
            response = commandFactory.createResponseCommand(request, RpcStatus.SERIALIZATION_ERROR);
        }

        ctx.writeAndFlush(response);
    }
}