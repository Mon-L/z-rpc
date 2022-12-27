package cn.zcn.rpc.remoting;

public interface RequestHandler<T> {

    /**
     * 可处理的 class
     */
    String acceptableClass();

    /**
     * 丢弃超时请求
     */
    boolean ignoredTimeoutRequest();

    void run(InvocationContext ctx, T obj);
}
