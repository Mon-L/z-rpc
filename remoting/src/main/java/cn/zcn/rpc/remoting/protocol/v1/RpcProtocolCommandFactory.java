package cn.zcn.rpc.remoting.protocol.v1;

import cn.zcn.rpc.remoting.protocol.*;

public class RpcProtocolCommandFactory implements CommandFactory {

    private final ProtocolCode protocolCode;

    protected RpcProtocolCommandFactory(ProtocolCode protocolCode) {
        this.protocolCode = protocolCode;
    }

    @Override
    public HeartbeatCommand createHeartbeatCommand() {
        return new HeartbeatCommand(protocolCode);
    }

    @Override
    public HeartbeatAckCommand createHeartbeatAckCommand() {
        return new HeartbeatAckCommand(protocolCode);
    }

    @Override
    public RequestCommand createRequestCommand(CommandType commandType, CommandCode commandCode) {
        RequestCommand request = new RequestCommand(protocolCode, commandType);
        request.setCommandCode(commandCode);
        return request;
    }

    @Override
    public ResponseCommand createResponseCommand(ICommand request, RpcStatus status) {
        if (request.getCommandType() == CommandType.REQUEST_ONEWAY) {
            return null;
        }

        ResponseCommand response = new ResponseCommand(protocolCode);
        response.setId(response.getId());
        response.setCommandCode(CommandCode.RESPONSE);
        response.setProtocolSwitch(request.getProtocolSwitch());
        response.setSerializer(request.getSerializer());
        response.setStatus(status);
        return response;
    }
}
