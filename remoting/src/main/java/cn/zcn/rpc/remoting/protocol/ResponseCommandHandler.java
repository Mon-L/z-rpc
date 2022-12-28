package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.CommandHandler;
import cn.zcn.rpc.remoting.InvokePromise;
import cn.zcn.rpc.remoting.RpcContext;
import cn.zcn.rpc.remoting.connection.Connection;
import cn.zcn.rpc.remoting.utils.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseCommandHandler implements CommandHandler<ResponseCommand> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseCommandHandler.class);

    @Override
    public void handle(RpcContext context, ResponseCommand command) {
        Connection conn = context.getChannelContext().channel().attr(Connection.CONNECTION_KEY).get();

        InvokePromise<ResponseCommand> promise = conn.removePromise(command.getId());
        if (promise != null) {
            promise.cancelTimeout();
            promise.setSuccess(command);
        } else {
            LOGGER.warn("Can not find invokePromise with id {} from connection {}, may be response timeout.",
                    command.getId(), NetUtil.getRemoteAddress(conn.getChannel()));
        }
    }
}
