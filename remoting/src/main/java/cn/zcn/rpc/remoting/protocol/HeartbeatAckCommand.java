package cn.zcn.rpc.remoting.protocol;

public class HeartbeatAckCommand extends ResponseCommand {

    public HeartbeatAckCommand(ProtocolCode protocolCode) {
        super(protocolCode);
        setCommandType(CommandType.RESPONSE);
        setCommandCode(CommandCode.HEARTBEAT);
    }
}
