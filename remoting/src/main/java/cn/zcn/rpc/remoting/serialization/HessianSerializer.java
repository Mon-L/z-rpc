package cn.zcn.rpc.remoting.serialization;

import cn.zcn.rpc.remoting.exception.SerializationException;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer implements Serializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HessianSerializer.class);

    private final SerializerFactory serializerFactory = new SerializerFactory();

    @Override
    public byte[] serialize(Object obj) throws SerializationException {
        Hessian2Output hessian = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            hessian = new Hessian2Output(out);
            hessian.setSerializerFactory(serializerFactory);
            hessian.writeObject(obj);
            return out.toByteArray();
        } catch (IOException e) {
            throw new SerializationException(e, "Exception occurred when hessian serializer object.");
        } finally {
            if (hessian != null) {
                try {
                    hessian.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close hessian.", e);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, String clazz) throws SerializationException {
        Hessian2Input hessian = null;
        try {
            ByteArrayInputStream out = new ByteArrayInputStream(bytes);
            hessian = new Hessian2Input(out);
            hessian.setSerializerFactory(serializerFactory);
            Object obj = hessian.readObject();
            return (T) obj;
        } catch (IOException e) {
            throw new SerializationException(e, "Exception occurred when hessian deserialize bytes.");
        } finally {
            if (hessian != null) {
                try {
                    hessian.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close hessian.", e);
                }
            }
        }
    }
}
