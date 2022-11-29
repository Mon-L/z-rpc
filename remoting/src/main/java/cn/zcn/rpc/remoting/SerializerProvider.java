package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.serialization.Serializer;

/**
 * 序列化器提供者
 */
public interface SerializerProvider {

    /**
     * 获取序列化器
     *
     * @param code serializer code
     * @return {@link Serializer}
     */
    Serializer getSerializer(byte code);

    /**
     * 获取默认序列化器
     *
     * @return serializer code
     */
    byte getDefaultSerializerCode();
}
