package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.CommandHandler;
import cn.zcn.rpc.remoting.RequestDispatcher;
import cn.zcn.rpc.remoting.RpcContext;
import cn.zcn.rpc.remoting.utils.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 请求处理器，将 {@code RequestCommand} 交由 {@link RequestDispatcher} 进行分发。
 *
 * @author zicung
 */
public class RequestCommandHandler implements CommandHandler<RequestCommand> {

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestCommandHandler.class);

    @Override
    public void handle(RpcContext ctx, RequestCommand command) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received request. Request Id:{}, From:{}", command.getId(),
                    NetUtil.getRemoteAddress(ctx.getChannelContext().channel()));
        }

        ctx.getRequestDispatcher().dispatch(ctx, command);
    }
}