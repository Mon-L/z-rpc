package cn.zcn.rpc.remoting;

import io.netty.util.Timeout;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

public interface InvokePromise<T> extends Promise<T> {
    void cancelTimeout();

    void setTimeout(Timeout timeout);
}
