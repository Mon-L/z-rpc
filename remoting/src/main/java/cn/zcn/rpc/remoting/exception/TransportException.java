package cn.zcn.rpc.remoting.exception;

public class TransportException extends BaseRuntimeException {
    public TransportException(String msg) {
        super(msg);
    }

    public TransportException(Throwable t, String msg) {
        super(t, msg);
    }

    public TransportException(String pattern, Object... args) {
        super(pattern, args);
    }

    public TransportException(Throwable t, String pattern, Object... args) {
        super(t, pattern, args);
    }
}
