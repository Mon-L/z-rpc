package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.ICommand;

/**
 * 命令处理器
 *
 * @author zicung
 */
public interface CommandHandler<T extends ICommand> {
    /**
     * 处理命令
     *
     * @param context Rpc 上下文
     * @param command 命令
     */
    void handle(CommandContext context, T command);
}
