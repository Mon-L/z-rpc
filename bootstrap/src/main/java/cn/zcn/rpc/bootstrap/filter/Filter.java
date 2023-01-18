package cn.zcn.rpc.bootstrap.filter;

import cn.zcn.rpc.bootstrap.RpcException;

/**
 * 调用过滤器
 *
 * @author zicung
 */
interface Filter<T extends Invocation> {

    /**
     * 过滤请求
     *
     * @param invocation 当前调用
     * @param filterContext 过滤器上下文
     * @throws RpcException 执行异常
     */
    void doFilter(T invocation, FilterContext<T> filterContext) throws RpcException;
}