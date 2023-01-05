package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.CommandHandler;
import cn.zcn.rpc.remoting.InvocationPromise;
import cn.zcn.rpc.remoting.RpcContext;
import cn.zcn.rpc.remoting.connection.Connection;
import cn.zcn.rpc.remoting.utils.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 响应命令处理器，根据 {@code ResponseCommand} 的 id 获取对应的 {@code InvocationPromise} 并设置
 * 响应结果。
 *
 * @author zicung
 */
public class ResponseCommandHandler implements CommandHandler<ResponseCommand> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseCommandHandler.class);

    @Override
    public void handle(RpcContext context, ResponseCommand command) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received response. Response Id:{}, From:{}", command.getId(),
                    NetUtil.getRemoteAddress(context.getChannelContext().channel()));
        }

        Connection conn = context.getChannelContext().channel().attr(Connection.CONNECTION_KEY).get();

        InvocationPromise<ResponseCommand> promise = conn.removePromise(command.getId());
        if (promise != null) {
            promise.cancelTimeout();
            promise.setSuccess(command);
        } else {
            LOGGER.warn("Can not find InvocationPromise with id {} from connection {}, may be response timeout.",
                    command.getId(), NetUtil.getRemoteAddress(conn.getChannel()));
        }
    }
}
