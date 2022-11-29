package cn.zcn.rpc.remoting.protocol;

public class ResponseCommand extends Command {

    private RpcStatus status;

    public ResponseCommand(ProtocolCode protocolCode) {
        super(protocolCode, CommandType.RESPONSE);
    }

    public RpcStatus getStatus() {
        return status;
    }

    public void setStatus(RpcStatus status) {
        this.status = status;
    }
}
