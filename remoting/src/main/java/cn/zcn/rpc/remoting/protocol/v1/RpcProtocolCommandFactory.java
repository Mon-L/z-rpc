package cn.zcn.rpc.remoting.protocol.v1;

import cn.zcn.rpc.remoting.SerializerManager;
import cn.zcn.rpc.remoting.protocol.*;
import cn.zcn.rpc.remoting.utils.CommandIdGenerator;

/**
 * Rpc protocol CommandFactory
 *
 * @author zicung
 */
public class RpcProtocolCommandFactory implements CommandFactory {
    private final ProtocolCode protocolCode;

    protected RpcProtocolCommandFactory(ProtocolCode protocolCode) {
        this.protocolCode = protocolCode;
    }

    @Override
    public HeartbeatCommand createHeartbeatCommand() {
        HeartbeatCommand heartbeatCommand = new HeartbeatCommand(protocolCode);
        heartbeatCommand.setId(CommandIdGenerator.getInstance().nextId());
        heartbeatCommand.setProtocolSwitch(ProtocolSwitch.parse((byte) 0));
        heartbeatCommand.setSerializer(SerializerManager.getInstance().getDefaultSerializerCode());
        return heartbeatCommand;
    }

    @Override
    public HeartbeatAckCommand createHeartbeatAckCommand(ICommand request) {
        HeartbeatAckCommand heartbeatAckCommand = new HeartbeatAckCommand(protocolCode);
        heartbeatAckCommand.setId(request.getId());
        heartbeatAckCommand.setSerializer(request.getSerializer());
        heartbeatAckCommand.setProtocolSwitch(request.getProtocolSwitch());
        heartbeatAckCommand.setStatus(RpcStatus.OK);
        return heartbeatAckCommand;
    }

    @Override
    public RequestCommand createRequestCommand(CommandType commandType, CommandCode commandCode) {
        RequestCommand request = new RequestCommand(protocolCode, commandType);
        request.setId(CommandIdGenerator.getInstance().nextId());
        request.setCommandCode(commandCode);
        return request;
    }

    @Override
    public ResponseCommand createResponseCommand(ICommand request, RpcStatus status) {
        if (request.getCommandType() == CommandType.REQUEST_ONEWAY) {
            return null;
        }

        ResponseCommand response = new ResponseCommand(protocolCode);
        response.setId(request.getId());
        response.setCommandCode(CommandCode.RESPONSE);
        response.setProtocolSwitch(request.getProtocolSwitch());
        response.setSerializer(request.getSerializer());
        response.setStatus(status);
        return response;
    }
}
