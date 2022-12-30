package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.extension.ExtensionPoint;
import cn.zcn.rpc.bootstrap.registry.Provider;

import java.util.List;

@ExtensionPoint
public interface LoadBalance {
    Provider select(List<Provider> providers, RpcRequest request);
}
