package cn.zcn.rpc.remoting.config;

import static cn.zcn.rpc.remoting.config.EnvConfigs.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RPC 选项集合，包含服务端、客户端通用选项。
 *
 * @author zicung
 */
public class RpcOptions implements Options {

    public static final Option<Boolean> NETTY_EPOLL = Option.valueOf("rpc.netty.epoll",
        getBool("rpc.netty.epoll", true));

    public static final Option<Integer> SO_BACKLOG = Option.valueOf("rpc.so.backlog",
        getInteger("rpc.so.backlog", 1024));

    public static final Option<Boolean> SO_REUSEADDR = Option.valueOf("rpc.so.reuseaddr",
        getBool("rpc.so.reuseaddr", true));

    public static final Option<Boolean> SO_KEEPALIVE = Option.valueOf("rpc.so.keepalive",
        getBool("rpc.so.keepalive", true));

    public static final Option<Boolean> TCP_NODELAY = Option.valueOf("rpc.so.nodelay",
        getBool("rpc.so.nodelay", true));

    public static final Option<Integer> SO_SNDBUF = Option.valueOf("rpc.so.sndbuf",
        getInteger("rpc.so.sndbuf"));

    public static final Option<Integer> SO_RCVBUF = Option.valueOf("rpc.so.rcvbuf",
        getInteger("rpc.so.rcvbuf"));

    public static final Option<Boolean> SYNC_SHUTDOWN = Option.valueOf("rpc.sync.shutdown",
        getBool("rpc.sync.shutdown", false));

    public static final Option<Integer> LOW_WRITE_BUFFER_WATER_MARK = Option.valueOf("rpc.low.water.mark",
        getInteger("rpc.low.water.mark", 32 * 1024));

    public static final Option<Integer> HIGH_WRITE_BUFFER_WATER_MARK = Option.valueOf("rpc.low.water.mark",
        getInteger("rpc.low.water.mark", 64 * 1024));

    public static final Option<String> CHARSET = Option.valueOf("rpc.charset",
        getString("rpc.charset", "UTF-8"));

    public static final Option<Integer> PROCESSOR_CORE_SIZE = Option.valueOf("rpc.processor.core.size",
        getInteger("rpc.processor.core.size", Runtime.getRuntime().availableProcessors()));

    public static final Option<Integer> PROCESSOR_MAX_SIZE = Option.valueOf("rpc.processor.max.size",
        getInteger("rpc.processor.max.size", (Runtime.getRuntime().availableProcessors() + 1) * 2));

    public static final Option<Integer> PROCESSOR_KEEPALIVE = Option.valueOf("rpc.processor.keepalive",
        getInteger("rpc.processor.keepalive", 60));

    public static final Option<Integer> PROCESSOR_WORKER_QUEEN_SIZE = Option.valueOf("rpc.processor.worker.queen.size",
        getInteger("rpc.processor.worker.queen.size", 1024));

    private final ConcurrentMap<Option<?>, Object> options = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings({ "unchecked" })
    public <T> T getOption(Option<T> option) {
        Object value = options.get(option);
        if (value == null) {
            return option.getDefaultValue();
        }

        return (T) value;
    }

    /**
     * 设置选项值。
     *
     * @param option 选项
     * @param value 值
     * @param <T> 选项值的类型
     */
    public <T> void setOption(Option<T> option, T value) {
        options.put(option, value);
    }
}
