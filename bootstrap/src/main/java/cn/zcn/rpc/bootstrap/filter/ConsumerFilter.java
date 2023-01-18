package cn.zcn.rpc.bootstrap.filter;

import cn.zcn.rpc.bootstrap.extension.ExtensionPoint;

/**
 * 用于在服务消费者调用过程中的过滤器
 *
 * @author zicung
 */
@ExtensionPoint
public interface ConsumerFilter extends Filter<ConsumerInvocation> {
}