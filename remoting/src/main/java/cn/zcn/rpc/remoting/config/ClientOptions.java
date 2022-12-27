package cn.zcn.rpc.remoting.config;

public class ClientOptions extends RpcOptions {
    public static final Option<Boolean> USE_CRC32 = Option.valueOf("rpc.use.crc32", true);

    public static final Option<Integer> CONNECT_TIMEOUT_MILLIS = Option.valueOf("rpc.connect.timeout", 10000);
}
