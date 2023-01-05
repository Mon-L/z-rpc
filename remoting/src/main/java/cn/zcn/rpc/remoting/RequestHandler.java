package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.RequestCommand;

/**
 * 请求处理器
 *
 * @author zicung
 */
public interface RequestHandler<T> {

    /**
     * 可处理的请求的class，对应 {@link RequestCommand#getClazz()}
     *
     * @return 类名
     */
    String acceptableClass();

    /**
     * 丢弃超时请求
     *
     * @return {@code true}, 丢弃超时请求；{@code false}，不丢弃超时请求
     */
    boolean ignoredTimeoutRequest();

    /**
     * 处理请求
     *
     * @param ctx 上下文
     * @param obj 请求
     */
    void handle(InvocationContext ctx, T obj);
}
