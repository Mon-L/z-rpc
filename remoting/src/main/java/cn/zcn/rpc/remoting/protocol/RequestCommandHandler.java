package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.CommandHandler;
import cn.zcn.rpc.remoting.CommandContext;
import cn.zcn.rpc.remoting.utils.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将 {@code RequestCommand} 交由 {@code RequestCommandDispatcher} 进行处理。
 *
 * @author zicung
 */
public class RequestCommandHandler implements CommandHandler<RequestCommand> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestCommandHandler.class);

    @Override
    public void handle(CommandContext context, RequestCommand command) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                "Received request. Request Id:{}, From:{}",
                command.getId(),
                NetUtil.getRemoteAddress(context.getChannelContext().channel()));
        }

        context.getRequestCommandDispatcher().dispatch(context, command);
    }
}
