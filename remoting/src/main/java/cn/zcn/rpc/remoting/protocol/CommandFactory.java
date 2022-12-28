package cn.zcn.rpc.remoting.protocol;

public interface CommandFactory {

    <T extends ICommand> T createHeartbeatCommand();

    <T extends ICommand> T createHeartbeatAckCommand(ICommand request);

    <T extends ICommand> T createRequestCommand(CommandType commandType, CommandCode commandCode);

    <T extends ICommand> T createResponseCommand(ICommand request, RpcStatus status);
}
