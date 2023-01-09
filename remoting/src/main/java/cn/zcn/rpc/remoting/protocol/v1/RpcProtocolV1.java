package cn.zcn.rpc.remoting.protocol.v1;

import cn.zcn.rpc.remoting.AbstractProtocol;
import cn.zcn.rpc.remoting.ProtocolDecoder;
import cn.zcn.rpc.remoting.ProtocolEncoder;
import cn.zcn.rpc.remoting.protocol.*;

/**
 * Rpc protocol v1.
 *
 * <pre>
 * Request
 * 0           1           2           3           4           5           6           7           8           9           10
 * +-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |protoc code| protoc ver|    type   |        cmd code       |                      id                       | serializer|
 * +-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |   switch  |                   timeout                     |      class length     |             content-              |
 * +-----------+-----------+-----------+-----------+-----------+                                                           +
 * | -length   |                                               class + content                                             |
 * +                                                                       +-----------+-----------+-----------+-----------+
 * |                                                                       |                    crc32                      |
 * +-----------------------------------------------------------------------------------------------------------------------+
 *
 * Response
 * 0           1           2           3           4           5           6           7           8           9           10
 * +-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |protoc code| protoc ver|    type   |        cmd code       |                       id                      | serializer|
 * +-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+           +
 * |   switch  |   response status     |      class length     |                content length                 |           +
 * +-----------+-----------+-----------+-----------+-----------+                                                           |
 * |                                               class + content                                                         |
 * +                                                                       +-----------+-----------+-----------+-----------+
 * |                                                                       |                   crc32                       |
 * +-----------------------------------------------------------------------------------------------------------------------+
 * </pre>
 *
 * @author zicung
 */
public class RpcProtocolV1 extends AbstractProtocol {
    public static final ProtocolCode PROTOCOL_CODE = ProtocolCode.from((byte) 1, (byte) 0);
    public static final int MIN_MESSAGE_LENGTH = 19;
    public static final int MIN_REQUEST_LENGTH = 21;
    public static final int MIN_RESPONSE_LENGTH = 19;

    private final ProtocolDecoder decoder = new RpcProtocolDecoder();
    private final ProtocolEncoder encoder = new RpcProtocolEncoder();
    private final CommandFactory commandFactory = new RpcProtocolCommandFactory(PROTOCOL_CODE);
    private final HeartbeatTrigger heartbeatTrigger = new DefaultHeartbeatTrigger(commandFactory);

    public RpcProtocolV1() {
        super(PROTOCOL_CODE);
        registerCommandHandler(CommandCode.HEARTBEAT, new HeartbeatCommandHandler());
        registerCommandHandler(CommandCode.REQUEST, new RequestCommandHandler());
        registerCommandHandler(CommandCode.RESPONSE, new ResponseCommandHandler());
    }

    @Override
    public HeartbeatTrigger getHeartbeatTrigger() {
        return heartbeatTrigger;
    }

    @Override
    public ProtocolEncoder getEncoder() {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder() {
        return decoder;
    }

    @Override
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }
}
