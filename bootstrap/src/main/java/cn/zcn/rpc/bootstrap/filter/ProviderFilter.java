package cn.zcn.rpc.bootstrap.filter;

import cn.zcn.rpc.bootstrap.extension.ExtensionPoint;

/**
 * 用于在服务提供者调用过程中的过滤器
 *
 * @author zicung
 */
@ExtensionPoint
public interface ProviderFilter extends Filter<ProviderInvocation> {

}