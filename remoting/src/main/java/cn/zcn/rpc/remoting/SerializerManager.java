package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.serialization.HessianSerializer;
import cn.zcn.rpc.remoting.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class SerializerManager implements SerializerProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializerManager.class);

    private final ConcurrentMap<Byte, Serializer> serializers = new ConcurrentHashMap<>();

    private volatile byte defaultSerializerCode;

    SerializerManager() {
        registerSerializer((byte) 1, new HessianSerializer());

        setDefaultSerializerCode((byte) 1);
    }

    /**
     * 注册序列化器
     *
     * @param code       serializer code
     * @param serializer serializer
     */
    public void registerSerializer(byte code, Serializer serializer) {
        if (serializer == null) {
            throw new IllegalArgumentException("Serializer should not be null!");
        }

        Serializer exist = serializers.put(code, serializer);
        if (exist != null) {
            LOGGER.warn("Replace serializer by code {}", code);
        }
    }

    /**
     * 卸载序列化器
     *
     * @param code serializer code
     */
    public void unregisterSerializer(byte code) {
        if (defaultSerializerCode == code) {
            throw new IllegalArgumentException("Should not unregister default serializer.");
        }

        serializers.remove(code);
    }

    @Override
    public Serializer getSerializer(byte code) {
        return serializers.get(code);
    }

    @Override
    public byte getDefaultSerializerCode() {
        return defaultSerializerCode;
    }

    public void setDefaultSerializerCode(byte code) {
        if (!serializers.containsKey(code)) {
            throw new IllegalArgumentException("Unknown serializer: " + code);
        } else {
            defaultSerializerCode = code;
        }
    }
}
