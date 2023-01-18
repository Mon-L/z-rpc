package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.serialization.HessianSerializer;
import cn.zcn.rpc.remoting.serialization.Serializer;

/**
 * Manage all serializers.
 * 
 * @author zicung
 */
public class SerializerManager {

	public static final byte HESSIAN = 1;
	public static byte DEFAULT_SERIALIZER = HESSIAN;

	private static Serializer[] SERIALIZERS = new Serializer[5];

	static {
		registerSerializer(HESSIAN, new HessianSerializer());
	}

	public static void registerSerializer(int code, Serializer serializer) {
		if (SERIALIZERS.length <= code) {
			Serializer[] newSerializers = new Serializer[code + 5];
			System.arraycopy(SERIALIZERS, 0, newSerializers, 0,
					SERIALIZERS.length);
			SERIALIZERS = newSerializers;
		}

		SERIALIZERS[code] = serializer;
	}

	public static Serializer getSerializer(int code) {
		if (code >= SERIALIZERS.length) {
			return null;
		}

		return SERIALIZERS[code];
	}
}
