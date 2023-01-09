package cn.zcn.rpc.bootstrap.consumer.filter;

import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.extension.ExtensionPoint;
import cn.zcn.rpc.bootstrap.registry.Provider;

/**
 * 请求过滤器
 *
 * @author zicung
 */
@ExtensionPoint
public interface Filter {

    /**
     * 运行过滤器的逻辑。
     *
     * <pre>
     * 使用 {@code nextFilter} 调用下一个过滤器。
     * 如果需要中断请求直接需要返回 {@code RpcResponse}。
     * </pre>
     *
     * @param provider 被选中的服务提供者
     * @param request 当前请求
     * @param nextFilter 下一个 {@code Filter}
     * @return {@code RpcResponse}
     * @throws RpcException 异常
     */
    RpcResponse doFilter(Provider provider, RpcRequest request, FilterChainNode nextFilter) throws RpcException;
}
