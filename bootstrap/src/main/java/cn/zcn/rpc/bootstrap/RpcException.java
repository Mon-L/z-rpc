package cn.zcn.rpc.bootstrap;

import cn.zcn.rpc.remoting.exception.BaseRuntimeException;

public class RpcException extends BaseRuntimeException {
    public RpcException(String msg) {
        super(msg);
    }

    public RpcException(Throwable t, String msg) {
        super(t, msg);
    }

    public RpcException(String pattern, Object... args) {
        super(pattern, args);
    }

    public RpcException(Throwable t, String pattern, Object... args) {
        super(t, pattern, args);
    }
}
