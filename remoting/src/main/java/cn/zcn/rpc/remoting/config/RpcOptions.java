package cn.zcn.rpc.remoting.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * RPC 选项集合。
 *
 * @author zicung
 */
public class RpcOptions implements Options {
    public static final Option<Boolean> NETTY_EPOLL = Option.valueOf("rpc.netty.epoll", true);

    public static final Option<Integer> SO_BACKLOG = Option.valueOf("rpc.so.backlog", 1024);

    public static final Option<Boolean> SO_REUSEADDR = Option.valueOf("rpc.so.reuseaddr", true);

    public static final Option<Boolean> SO_KEEPALIVE = Option.valueOf("rpc.so.keeplive", true);

    public static final Option<Boolean> TCP_NODELAY = Option.valueOf("rpc.so.nodelay", true);

    public static final Option<Integer> SO_SNDBUF = Option.valueOf("rpc.so.sndbuf", null);

    public static final Option<Integer> SO_RCVBUF = Option.valueOf("rpc.so.rcvbuf", null);

    public static final Option<Boolean> SYNC_SHUTDOWN = Option.valueOf("rpc.sync.shutdown", false);

    public static final Option<Charset> CHARSET = Option.valueOf("rpc.charsets", StandardCharsets.UTF_8);

    public static final Option<Integer> PROCESSOR_CORE_SIZE = Option.valueOf("rpc.processor.core.size",
        Runtime.getRuntime().availableProcessors());

    public static final Option<Integer> PROCESSOR_MAX_SIZE = Option.valueOf("rpc.processor.max.size",
        (Runtime.getRuntime().availableProcessors() + 1) * 2);

    public static final Option<Integer> PROCESSOR_KEEPALIVE = Option.valueOf("rpc.processor.keepalive", 60);

    public static final Option<Integer> PROCESSOR_WORKER_QUEEN_SIZE = Option.valueOf("rpc.processor.worker.queen.size",
        1024);

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
