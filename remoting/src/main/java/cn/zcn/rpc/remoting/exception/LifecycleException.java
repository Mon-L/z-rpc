package cn.zcn.rpc.remoting.exception;

/** @author zicung */
public class LifecycleException extends PatternMessageException {

    public LifecycleException(String msg) {
        super(msg);
    }

    public LifecycleException(String msg, Throwable t) {
        super(msg, t);
    }

    public LifecycleException(String pattern, Object... args) {
        super(pattern, args);
    }
}
