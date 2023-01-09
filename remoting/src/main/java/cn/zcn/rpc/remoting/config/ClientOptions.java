package cn.zcn.rpc.remoting.config;

/**
 * {@code ClientOptions} 包含客户端可配置的所有选项。
 *
 * @author zicung
 */
public class ClientOptions extends RpcOptions {
    public static final Option<Boolean> USE_CRC32 = Option.valueOf("rpc.use.crc32", true);

    public static final Option<Integer> CONNECT_TIMEOUT_MILLIS = Option.valueOf("rpc.connect.timeout", 10000);

    /** 是否开启连接空闲检测 */
    public static final Option<Boolean> CHECK_IDLE_STATE = Option.valueOf("rpc.check.idle", true);

    /** 心跳间隔 */
    public static final Option<Integer> HEARTBEAT_INTERVAL_MILLIS = Option.valueOf("rpc.heartbeat.interval", 15000);

    /** 心跳最大失败次数 */
    public static final Option<Integer> HEARTBEAT_MAX_FAILURES = Option.valueOf("rpc.heartbeat.max.failures", 3);
}
