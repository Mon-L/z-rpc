package cn.zcn.rpc.remoting.exception;

/**
 * @author zicung
 */
public class LifecycleException extends BaseRuntimeException {

    public LifecycleException(String msg) {
        super(msg);
    }

    public LifecycleException(Throwable t, String msg) {
        super(t, msg);
    }

    public LifecycleException(String pattern, Object... args) {
        super(pattern, args);
    }

    public LifecycleException(Throwable t, String pattern, Object... args) {
        super(t, pattern, args);
    }
}
