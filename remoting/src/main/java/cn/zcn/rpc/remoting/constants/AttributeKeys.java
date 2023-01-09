package cn.zcn.rpc.remoting.constants;

import cn.zcn.rpc.remoting.Url;
import cn.zcn.rpc.remoting.config.Options;
import cn.zcn.rpc.remoting.connection.Connection;
import cn.zcn.rpc.remoting.protocol.ProtocolCode;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.nio.channels.Channel;

/**
 * 用于访问 {@link Channel} 中的 {@link Attribute} 的键的集合。
 *
 * @author zicung
 */
public class AttributeKeys {
    /** 用于访问 {@code Channel} 中的 {@link Connection} 实例的键*/
    public static final AttributeKey<Connection> CONNECTION = AttributeKey.valueOf("rpc-connection");

    /** 用于访问 {@code Channel} 中的 {@link Url} 实例的键*/
    public static final AttributeKey<Url> CONNECTION_URL = AttributeKey.valueOf("connection-url");

    /** 用于访问 {@code Channel} 中的 {@link ProtocolCode} 实例的键*/
    public static final AttributeKey<ProtocolCode> PROTOCOL = AttributeKey.valueOf("protocol");

    /** 用于访问 {@code Channel} 中的 {@link Options} 实例的键*/
    public static final AttributeKey<Options> OPTIONS = AttributeKey.valueOf("options");
}
