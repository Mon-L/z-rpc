package cn.zcn.rpc.bootstrap.consumer.proxy;

import cn.zcn.rpc.bootstrap.consumer.RpcInvoker;
import cn.zcn.rpc.bootstrap.extension.ExtensionPoint;

/**
 * 接口代理创建者
 *
 * @author zicung
 */
@ExtensionPoint
public interface Proxy {

    /**
     * 创建接口代理
     *
     * @param clazz   被代理的接口
     * @param rpcInvoker 远程服务调用者
     * @return 被代理的接口实例
     */
    <T> T createProxy(Class<?> clazz, RpcInvoker rpcInvoker);
}
