package cn.zcn.rpc.remoting.exception;

public class ProtocolException extends BaseRuntimeException {

    public ProtocolException(String msg) {
        super(msg);
    }

    public ProtocolException(Throwable t, String msg) {
        super(t, msg);
    }

    public ProtocolException(String pattern, Object... args) {
        super(pattern, args);
    }

    public ProtocolException(Throwable t, String pattern, Object... args) {
        super(t, pattern, args);
    }
}
