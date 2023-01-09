package cn.zcn.rpc.remoting.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Named thread factory.
 *
 * <p>使用 {@code prefix} 和递增的 {@code num} 对 {@code Thread} 进行命名，格式如下：
 *
 * <pre>
 * {prefix} + "-" + {num}
 * </pre>
 *
 * @author zicung
 */
public class NamedThreadFactory implements ThreadFactory {
    private final String prefix;
    private final boolean daemon;
    private final AtomicInteger num = new AtomicInteger(0);

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NamedThreadFactory(String prefix, boolean daemon) {
        this.prefix = prefix + "-";
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, prefix + num.incrementAndGet());
        thread.setDaemon(daemon);
        return thread;
    }
}
