package cn.zcn.rpc.bootstrap.consumer.filter;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.registry.Provider;

public class FilterChainNode {

    private final Filter filter;
    private final FilterChainNode next;

    protected FilterChainNode(Filter filter, FilterChainNode next) {
        this.filter = filter;
        this.next = next;
    }

    public RpcResponse invoke(Provider provider, RpcRequest request) {
        return filter.doFilter(provider, request, next);
    }
}