package cn.zcn.rpc.remoting.protocol;

/** @author zicung */
public class RequestCommand extends BaseCommand {
    private int timeout;

    public RequestCommand(ProtocolCode protocolCode, CommandType commandType) {
        super(protocolCode, commandType);
        setCommandCode(CommandCode.REQUEST);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }
}
