package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.extension.ExtensionPoint;
import cn.zcn.rpc.bootstrap.registry.Provider;

import java.util.List;

/**
 * 负载均衡器，在服务提供者列表中选出一个服务提供者。
 *
 * @author zicung
 */
@ExtensionPoint
public interface LoadBalance {

    /**
     * 选择服务提供者
     *
     * @param providers 服务提供者列表
     * @param request   请求
     * @return 被选中的服务提供者
     */
    Provider select(List<Provider> providers, RpcRequest request);
}
