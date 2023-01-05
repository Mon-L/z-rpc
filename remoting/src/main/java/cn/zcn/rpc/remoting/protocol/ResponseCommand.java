package cn.zcn.rpc.remoting.protocol;

/**
 * @author zicung
 */
public class ResponseCommand extends BaseCommand {

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
