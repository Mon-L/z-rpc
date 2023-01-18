package cn.zcn.rpc.bootstrap;

import cn.zcn.rpc.remoting.exception.PatternMessageException;

/** @author zicung */
public class RpcException extends PatternMessageException {

    public RpcException(String msg) {
        super(msg);
    }

    public RpcException(String msg, Throwable t) {
        super(msg, t);
    }

    public RpcException(String pattern, Object... args) {
        super(pattern, args);
    }
}
