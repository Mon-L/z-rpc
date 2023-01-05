package cn.zcn.rpc.bootstrap.consumer.filter;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.registry.Provider;

/**
 * 过滤器链节点。用于维护当前节点的 {@code  Filter} 和下一个 {@code FilterChainNode}。
 *
 * @author zicung
 */
public class FilterChainNode {

    private final Filter filter;
    private final FilterChainNode next;

    protected FilterChainNode(Filter filter, FilterChainNode next) {
        this.filter = filter;
        this.next = next;
    }

    /**
     * 调用下一个过滤器
     *
     * @param provider 被选中的服务提供者
     * @param request  当前请求
     * @return {@code RpcResponse}
     */
    public RpcResponse invoke(Provider provider, RpcRequest request) {
        return filter.doFilter(provider, request, next);
    }
}