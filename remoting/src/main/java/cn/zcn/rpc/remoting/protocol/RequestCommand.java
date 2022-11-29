package cn.zcn.rpc.remoting.protocol;

public class RequestCommand extends Command {

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
