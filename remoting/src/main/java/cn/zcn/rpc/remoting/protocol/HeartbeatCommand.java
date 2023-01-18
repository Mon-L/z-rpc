package cn.zcn.rpc.remoting.protocol;

/** @author zicung */
public class HeartbeatCommand extends RequestCommand {

	public HeartbeatCommand(ProtocolCode protocolCode) {
		super(protocolCode, CommandType.REQUEST);
		setCommandCode(CommandCode.HEARTBEAT);
	}
}
