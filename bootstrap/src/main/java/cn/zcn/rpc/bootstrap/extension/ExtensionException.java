package cn.zcn.rpc.bootstrap.extension;

import cn.zcn.rpc.remoting.exception.BaseRuntimeException;

/** @author zicung */
public class ExtensionException extends BaseRuntimeException {

    public ExtensionException(Throwable t, String msg) {
        super(t, msg);
    }

    public ExtensionException(String pattern, Object... args) {
        super(pattern, args);
    }
}
