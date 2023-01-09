package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.ProtocolCode;
import cn.zcn.rpc.remoting.protocol.v1.RpcProtocolV1;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage all protocol.
 *
 * @author zicung
 */
public class ProtocolManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolManager.class);

    private static final ProtocolManager INSTANCE = new ProtocolManager();

    public static ProtocolManager getInstance() {
        return INSTANCE;
    }

    private ProtocolCode defaultProtocol;
    private final ConcurrentMap<ProtocolCode, Protocol> protocols = new ConcurrentHashMap<>();

    private ProtocolManager() {
        registerProtocol(RpcProtocolV1.PROTOCOL_CODE, new RpcProtocolV1());
        setDefaultProtocol(RpcProtocolV1.PROTOCOL_CODE);
    }

    /**
     * 注册协议
     *
     * @param protocolCode 协议码
     * @param protocol 协议
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
     * 注销协议，无法注销默认协议。
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

    /**
     * 获取指定协议
     *
     * @param protocolCode 协议码
     * @return Protocol
     */
    public Protocol getProtocol(ProtocolCode protocolCode) {
        return protocols.get(protocolCode);
    }

    /**
     * 获取默认协议
     *
     * @return Protocol
     */
    public Protocol getDefaultProtocol() {
        return protocols.get(defaultProtocol);
    }

    /**
     * 设置默认协议
     *
     * @param protocolCode 默认协议码
     */
    public void setDefaultProtocol(ProtocolCode protocolCode) {
        if (protocolCode == null) {
            throw new IllegalArgumentException("Protocol code should not be null!");
        } else if (!protocols.containsKey(protocolCode)) {
            throw new IllegalArgumentException("Unknown protocol, " + protocolCode);
        } else {
            defaultProtocol = protocolCode;
        }
    }
}
