package cn.zcn.rpc.bootstrap.consumer.proxy;


import cn.zcn.rpc.bootstrap.consumer.Invoker;
import cn.zcn.rpc.bootstrap.extension.ExtensionPoint;

@ExtensionPoint
public interface Proxy {
    <T> T createProxy(Class<?> clazz, Invoker invoker);
}
