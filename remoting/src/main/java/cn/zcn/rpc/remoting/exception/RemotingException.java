package cn.zcn.rpc.remoting.exception;

/** @author zicung */
public class RemotingException extends PatternMessageException {

    public RemotingException(String msg) {
        super(msg);
    }

    public RemotingException(String msg, Throwable t) {
        super(msg, t);
    }

    public RemotingException(String pattern, Object... args) {
        super(pattern, args);
    }
}
