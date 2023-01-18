package cn.zcn.rpc.remoting.exception;

import org.slf4j.helpers.MessageFormatter;

/** @author zicung */
public abstract class PatternMessageException extends RuntimeException {

    private static Throwable getCause(Object[] argArray) {
        if (argArray == null || argArray.length == 0) {
            return null;
        }

        final Object lastEntry = argArray[argArray.length - 1];
        if (lastEntry instanceof Throwable) {
            return (Throwable) lastEntry;
        }

        return null;
    }

    public PatternMessageException(String msg) {
        super(msg);
    }

    public PatternMessageException(String msg, Throwable t) {
        super(msg, t);
    }

    public PatternMessageException(String pattern, Object... args) {
        super(MessageFormatter.arrayFormat(pattern, args).getMessage(), getCause(args));
    }
}
