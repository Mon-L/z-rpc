package cn.zcn.rpc.remoting.serialization;

import cn.zcn.rpc.remoting.exception.SerializationException;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * hessian serializer
 *
 * @author zicung
 */
public class HessianSerializer implements Serializer {
    private final SerializerFactory serializerFactory = new SerializerFactory();

    @Override
    public byte[] serialize(Object obj) throws SerializationException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Hessian2Output hessian = new Hessian2Output(out);
        hessian.setSerializerFactory(serializerFactory);

        try {
            hessian.writeObject(obj);
            hessian.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new SerializationException(e, "Exception occurred when hessian serializer object.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, String clazz) throws SerializationException {
        ByteArrayInputStream out = new ByteArrayInputStream(bytes);
        Hessian2Input hessian = new Hessian2Input(out);
        hessian.setSerializerFactory(serializerFactory);

        try {
            Object obj = hessian.readObject();
            hessian.close();
            return (T) obj;
        } catch (IOException e) {
            throw new SerializationException(e, "Exception occurred when hessian deserialize bytes.");
        }
    }
}
