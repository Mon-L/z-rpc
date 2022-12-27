package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;

public interface Invoker {
    RpcResponse invoke(RpcRequest request) throws RpcException;
}
