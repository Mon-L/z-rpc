package cn.zcn.rpc.bootstrap.consumer.filter;

import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.extension.ExtensionPoint;
import cn.zcn.rpc.bootstrap.registry.Provider;

@ExtensionPoint
public interface Filter {
    RpcResponse doFilter(Provider provider, RpcRequest request, FilterChainNode nextFilter) throws RpcException;
}
