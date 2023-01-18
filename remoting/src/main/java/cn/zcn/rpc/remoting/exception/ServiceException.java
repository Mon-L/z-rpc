package cn.zcn.rpc.remoting.exception;

/** @author zicung */
public class ServiceException extends PatternMessageException {

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(String msg, Throwable t) {
        super(msg, t);
    }

    public ServiceException(String pattern, Object... args) {
        super(pattern, args);
    }
}
