package cn.zcn.rpc.remoting.serialization;

import cn.zcn.rpc.remoting.exception.SerializationException;

/**
 * 序列化器，提供序列化对象和反序列化字节数组的功能。
 *
 * @author zicung
 */
public interface Serializer {

    /**
     * 将对象序列化成字节数组
     *
     * @param obj 待序列化对象
     * @return 字节数组
     * @throws SerializationException 序列化异常
     */
    byte[] serialize(Object obj) throws SerializationException;

    /**
     * 将字节数组反序列化为对象
     *
     * @param bytes 字节数组
     * @param clazz 类名
     * @param <T>   对象类型
     * @return 反序列化后的对象实例
     * @throws SerializationException 反序列化异常
     */
    <T> T deserialize(byte[] bytes, String clazz) throws SerializationException;
}
