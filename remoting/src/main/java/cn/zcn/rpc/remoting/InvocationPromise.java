package cn.zcn.rpc.remoting;

import io.netty.util.Timeout;
import io.netty.util.concurrent.Promise;

/**
 * Special invocation Future which is writable.
 *
 * @author zicung
 */
public interface InvocationPromise<T> extends Promise<T> {
    /**
     * 取消超时定时器
     */
    void cancelTimeout();

    /**
     * 设置超时定时器
     *
     * @param timeout 超时定时器
     */
    void setTimeout(Timeout timeout);
}
