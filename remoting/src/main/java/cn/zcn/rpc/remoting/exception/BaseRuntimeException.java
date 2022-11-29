package cn.zcn.rpc.remoting.exception;

import java.text.MessageFormat;

public class BaseRuntimeException extends RuntimeException {

    public BaseRuntimeException(String msg) {
        super(msg);
    }

    public BaseRuntimeException(Throwable t, String msg) {
        super(msg, t);
    }

    public BaseRuntimeException(String pattern, Object... args) {
        super(MessageFormat.format(pattern, args));
    }

    public BaseRuntimeException(Throwable t, String pattern, Object... args) {
        super(MessageFormat.format(pattern, args), t);
    }
}
