package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.ICommand;

/**
 * 命令处理器
 */
public interface CommandHandler<T extends ICommand> {

    /**
     * 处理命令
     *
     * @param context {@link RpcContext}
     * @param command 报文
     */
    void handle(RpcContext context, T command);
}
