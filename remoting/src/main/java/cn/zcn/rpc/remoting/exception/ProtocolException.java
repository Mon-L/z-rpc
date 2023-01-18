package cn.zcn.rpc.remoting.exception;

/** @author zicung */
public class ProtocolException extends PatternMessageException {

    public ProtocolException(String msg) {
        super(msg);
    }

    public ProtocolException(String msg, Throwable t) {
        super(msg, t);
    }

    public ProtocolException(String pattern, Object... args) {
        super(pattern, args);
    }
}
