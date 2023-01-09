package cn.zcn.rpc.bootstrap.registry;

import cn.zcn.rpc.remoting.exception.BaseRuntimeException;

/** @author zicung */
public class RegistryException extends BaseRuntimeException {

    public RegistryException(Throwable t, String msg) {
        super(t, msg);
    }

    public RegistryException(Throwable t, String pattern, Object... args) {
        super(t, pattern, args);
    }
}
