package cn.zcn.rpc.remoting.exception;

/**
 * @author zicung
 */
public class RemotingException extends BaseRuntimeException {

    public RemotingException(String msg) {
        super(msg);
    }

    public RemotingException(Throwable t, String msg) {
        super(t, msg);
    }

    public RemotingException(String pattern, Object... args) {
        super(pattern, args);
    }

    public RemotingException(Throwable t, String pattern, Object... args) {
        super(t, pattern, args);
    }
}
