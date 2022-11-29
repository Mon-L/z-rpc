package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.ProtocolCode;

public interface ProtocolProvider {
    /**
     * 获取指定协议
     *
     * @param protocolCode 协议码
     * @return Protocol
     */
    Protocol getProtocol(ProtocolCode protocolCode);

    /**
     * 获取默认协议
     *
     * @return Protocol
     */
    Protocol getDefaultProtocol();
}
