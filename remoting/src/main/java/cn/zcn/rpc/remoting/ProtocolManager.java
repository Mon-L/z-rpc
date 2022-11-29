package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.ProtocolCode;
import cn.zcn.rpc.remoting.protocol.v1.RpcProtocolV1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 协议管理者，维护支持的协议
 */
public class ProtocolManager implements ProtocolProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolManager.class);

    private ProtocolCode defaultProtocol;
    private final ConcurrentMap<ProtocolCode, Protocol> protocols = new ConcurrentHashMap<>();

    protected ProtocolManager() {
        registerProtocol(RpcProtocolV1.PROTOCOL_CODE, new RpcProtocolV1());

        this.defaultProtocol = RpcProtocolV1.PROTOCOL_CODE;
    }

    /**
     * 注册协议
     *
     * @param protocolCode 协议码
     * @param protocol     协议
     */
    public void registerProtocol(ProtocolCode protocolCode, Protocol protocol) {
        if (protocolCode == null || protocol == null) {
            throw new IllegalArgumentException("Both protocol code and protocol should not be null!");
        }

        Protocol exist = protocols.put(protocolCode, protocol);
        if (exist != null) {
            LOGGER.warn("Replace protocol by protocolCode: {}", protocolCode);
        }
    }

    /**
     * 注销协议
     *
     * @param protocolCode 协议码
     */
    public void unregisterProtocol(ProtocolCode protocolCode) {
        if (protocolCode == null) {
            throw new IllegalArgumentException("Protocol code should not be null!");
        }

        if (protocolCode.equals(defaultProtocol)) {
            throw new IllegalArgumentException("Should not unregister default protocol.");
        }

        protocols.remove(protocolCode);
    }

    @Override
    public Protocol getProtocol(ProtocolCode protocolCode) {
        return protocols.get(protocolCode);
    }

    @Override
    public Protocol getDefaultProtocol() {
        return protocols.get(defaultProtocol);
    }

    public void setDefaultProtocol(ProtocolCode protocolCode) {
        if (protocolCode == null) {
            throw new IllegalArgumentException("Protocol code should not be null!");
        } else if (!protocols.containsKey(protocolCode)) {
            throw new IllegalArgumentException("Unknown protocol " + protocolCode.toString());
        } else {
            defaultProtocol = protocolCode;
        }
    }
}
