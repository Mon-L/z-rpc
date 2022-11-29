package cn.zcn.rpc.remoting.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {

    private static final IDGenerator instance = new IDGenerator();

    private final AtomicInteger id = new AtomicInteger(0);

    private IDGenerator() {

    }

    public static IDGenerator getInstance() {
        return instance;
    }

    public int nextId() {
        return id.incrementAndGet();
    }
}
