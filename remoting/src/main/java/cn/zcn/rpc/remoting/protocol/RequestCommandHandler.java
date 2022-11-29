package cn.zcn.rpc.remoting.protocol;

import cn.zcn.rpc.remoting.CommandHandler;
import cn.zcn.rpc.remoting.RpcContext;

public class RequestCommandHandler implements CommandHandler<RequestCommand> {

    @Override
    public void handle(RpcContext ctx, RequestCommand command) {
        ctx.getRpcProcessor().execute(ctx, command);
    }
}