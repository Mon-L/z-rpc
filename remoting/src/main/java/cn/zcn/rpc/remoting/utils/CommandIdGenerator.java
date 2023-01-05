package cn.zcn.rpc.remoting.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ID 生成器
 *
 * @author zicung
 */
public class CommandIdGenerator {

    private static final CommandIdGenerator INSTANCE = new CommandIdGenerator();

    private final AtomicInteger id = new AtomicInteger(0);

    private CommandIdGenerator() {
    }

    public static CommandIdGenerator getInstance() {
        return INSTANCE;
    }

    /**
     * 获取下一个 id
     *
     * @return int
     */
    public int nextId() {
        return id.incrementAndGet();
    }
}
