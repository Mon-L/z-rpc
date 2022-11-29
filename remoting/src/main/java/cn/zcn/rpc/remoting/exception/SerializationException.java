package cn.zcn.rpc.remoting.exception;

public class SerializationException extends BaseRuntimeException {
    public SerializationException(String msg) {
        super(msg);
    }

    public SerializationException(Throwable t, String msg) {
        super(t, msg);
    }

    public SerializationException(String pattern, Object... args) {
        super(pattern, args);
    }

    public SerializationException(Throwable t, String pattern, Object... args) {
        super(t, pattern, args);
    }
}
