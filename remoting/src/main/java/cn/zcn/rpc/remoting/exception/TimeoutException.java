package cn.zcn.rpc.remoting.exception;

/** @author zicung */
public class TimeoutException extends PatternMessageException {

    public TimeoutException(String msg) {
        super(msg);
    }

    public TimeoutException(String msg, Throwable t) {
        super(msg, t);
    }

    public TimeoutException(String pattern, Object... args) {
        super(pattern, args);
    }
}
