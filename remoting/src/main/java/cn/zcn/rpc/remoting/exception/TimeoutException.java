package cn.zcn.rpc.remoting.exception;

/** @author zicung */
public class TimeoutException extends BaseRuntimeException {

    public TimeoutException(String msg) {
        super(msg);
    }

    public TimeoutException(Throwable t, String msg) {
        super(t, msg);
    }

    public TimeoutException(String pattern, Object... args) {
        super(pattern, args);
    }

    public TimeoutException(Throwable t, String pattern, Object... args) {
        super(t, pattern, args);
    }
}
