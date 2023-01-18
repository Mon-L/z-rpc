package cn.zcn.rpc.remoting.exception;

/** @author zicung */
public class TransportException extends PatternMessageException {

    public TransportException(String msg) {
        super(msg);
    }

    public TransportException(String msg, Throwable t) {
        super(msg, t);
    }

    public TransportException(String pattern, Object... args) {
        super(pattern, args);
    }
}
