package cn.zcn.rpc.remoting.exception;

/**
 * @author zicung
 */
public class ServiceException extends BaseRuntimeException{

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(Throwable t, String msg) {
        super(t, msg);
    }

    public ServiceException(String pattern, Object... args) {
        super(pattern, args);
    }

    public ServiceException(Throwable t, String pattern, Object... args) {
        super(t, pattern, args);
    }
}
