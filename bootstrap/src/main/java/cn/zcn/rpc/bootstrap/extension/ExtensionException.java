package cn.zcn.rpc.bootstrap.extension;

import cn.zcn.rpc.remoting.exception.PatternMessageException;

/** @author zicung */
public class ExtensionException extends PatternMessageException {

    public ExtensionException(String msg) {
        super(msg);
    }

    public ExtensionException(String msg, Throwable t) {
        super(msg, t);
    }

    public ExtensionException(String pattern, Object... args) {
        super(pattern, args);
    }
}
