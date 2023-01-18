package cn.zcn.rpc.bootstrap.registry;

import cn.zcn.rpc.remoting.exception.PatternMessageException;

/** @author zicung */
public class RegistryException extends PatternMessageException {

    public RegistryException(String msg) {
        super(msg);
    }

    public RegistryException(String msg, Throwable t) {
        super(msg, t);
    }

    public RegistryException(String pattern, Object... args) {
        super(pattern, args);
    }
}
