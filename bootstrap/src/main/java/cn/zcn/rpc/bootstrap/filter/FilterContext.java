package cn.zcn.rpc.bootstrap.filter;

import cn.zcn.rpc.bootstrap.RpcException;

/**
 * @author zicung
 */
public interface FilterContext<T extends Invocation> {

    /**
     * 调用 {@link FilterChain} 中的下一个过滤器
     *
     * @param invocation 当前调用
     * @throws RpcException 异常
     */
    void doFilter(T invocation) throws RpcException;

    /**
     * 获取当前 filter
     *
     * @return filter
     */
    Filter<T> filter();
}