package cn.zcn.rpc.bootstrap;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

public class RpcResponse implements Serializable {

    private static final int NORMAL = 1;
    private static final int EXCEPTIONAL = -1;

    private int state;
    private Object outcome;

    public void set(Object outcome) {
        this.outcome = outcome;
        this.state = NORMAL;
    }

    public void setException(Throwable t) {
        this.outcome = t;
        this.state = EXCEPTIONAL;
    }

    public Object get() throws ExecutionException {
        if (this.state == NORMAL) {
            return outcome;
        }

        throw new ExecutionException((Throwable) outcome);
    }
}
