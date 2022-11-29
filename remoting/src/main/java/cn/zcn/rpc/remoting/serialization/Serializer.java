package cn.zcn.rpc.remoting.serialization;

import cn.zcn.rpc.remoting.exception.SerializationException;

public interface Serializer {

    /**
     * 将对象序列化成字节数组
     *
     * @param obj 待序列化对象
     * @return 字节数组
     * @throws SerializationException 序列化失败异常
     */
    byte[] serialize(Object obj) throws SerializationException;

    /**
     * 将字节数组反序列化为对象
     *
     * @param bytes 字节数组
     * @param clazz 类名
     * @throws SerializationException 反序列化失败异常
     */
    <T> T deserialize(byte[] bytes, String clazz) throws SerializationException;
}
