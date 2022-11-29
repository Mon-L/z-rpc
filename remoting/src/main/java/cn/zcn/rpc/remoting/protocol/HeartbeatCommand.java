package cn.zcn.rpc.remoting.protocol;

public class HeartbeatCommand extends RequestCommand {

    public HeartbeatCommand(ProtocolCode protocolCode) {
        super(protocolCode, CommandType.REQUEST);
        setCommandCode(CommandCode.HEARTBEAT);
    }
}
