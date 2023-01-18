package cn.zcn.rpc.remoting.exception;

/** @author zicung */
public class SerializationException extends PatternMessageException {

    public SerializationException(String msg) {
        super(msg);
    }

    public SerializationException(String msg, Throwable t) {
        super(msg, t);
    }

    public SerializationException(String pattern, Object... args) {
        super(pattern, args);
    }
}
