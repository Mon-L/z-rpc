package cn.zcn.rpc.remoting.protocol;

/**
 * 命令工厂，提供命令的创建功能。
 *
 * @author zicung
 */
public interface CommandFactory {

    /**
     * 创建心跳命令
     *
     * @return {@code ICommand}
     */
    <T extends ICommand> T createHeartbeatCommand();

    /**
     * 创建心跳响应
     *
     * @param request 心跳请求
     * @return {@code ICommand}
     */
    <T extends ICommand> T createHeartbeatAckCommand(ICommand request);

    /**
     * 创建请求
     *
     * @param commandType 命令类型
     * @param commandCode 命令码
     * @return {@code ICommand}
     */
    <T extends ICommand> T createRequestCommand(CommandType commandType, CommandCode commandCode);

    /**
     * 创建响应
     *
     * @param request 请求
     * @param status  响应码
     * @return {@code ICommand}
     */
    <T extends ICommand> T createResponseCommand(ICommand request, RpcStatus status);
}
