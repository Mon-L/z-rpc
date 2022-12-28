package cn.zcn.rpc.remoting.config;

import io.netty.util.AttributeKey;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RpcOptions extends OptionsImpl {

    public static final AttributeKey<Options> OPTIONS_ATTRIBUTE_KEY = AttributeKey.valueOf("options");

    public static final Option<Boolean> NETTY_EPOLL = Option.valueOf("rpc.netty.epoll", true);

    public static final Option<Integer> SO_BACKLOG = Option.valueOf("rpc.so.backlog", 1024);

    public static final Option<Boolean> SO_REUSEADDR = Option.valueOf("rpc.so.reuseaddr", true);

    public static final Option<Boolean> SO_KEEPALIVE = Option.valueOf("rpc.so.keeplive", true);

    public static final Option<Boolean> TCP_NODELAY = Option.valueOf("rpc.so.nodelay", true);

    public static final Option<Integer> SO_SNDBUF = Option.valueOf("rpc.so.sndbuf", null);

    public static final Option<Integer> SO_RCVBUF = Option.valueOf("rpc.so.rcvbuf", null);

    public static final Option<Boolean> SYNC_SHUTDOWN = Option.valueOf("rpc.sync.shutdown", false);

    public static final Option<Charset> CHARSET = Option.valueOf("rpc.charsets", StandardCharsets.UTF_8);

    public static final Option<Integer> PROCESSOR_CORE_SIZE = Option.valueOf("rpc.processor.core.size", Runtime.getRuntime().availableProcessors());

    public static final Option<Integer> PROCESSOR_MAX_SIZE = Option.valueOf("rpc.processor.max.size", (Runtime.getRuntime().availableProcessors() + 1) * 2);

    public static final Option<Integer> PROCESSOR_KEEPALIVE = Option.valueOf("rpc.processor.keepalive", 60);

    public static final Option<Integer> PROCESSOR_WORKER_QUEEN_SIZE = Option.valueOf("rpc.processor.worker.queen.size", 1024);
}
