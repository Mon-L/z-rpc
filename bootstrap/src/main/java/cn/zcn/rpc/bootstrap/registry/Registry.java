package cn.zcn.rpc.bootstrap.registry;

import cn.zcn.rpc.bootstrap.RegistryConfig;
import cn.zcn.rpc.bootstrap.extension.ExtensionPoint;
import cn.zcn.rpc.bootstrap.provider.InterfaceConfig;
import cn.zcn.rpc.remoting.lifecycle.Lifecycle;

import java.util.Collection;

@ExtensionPoint
public interface Registry extends Lifecycle {

    void init(RegistryConfig registryConfig);

    void register(Collection<InterfaceConfig> interfaceConfig);

    void unregister(Collection<InterfaceConfig> interfaceConfig);

    void subscribe(InterfaceConfig interfaceConfig);

    void unsubscribe(InterfaceConfig interfaceConfig);
}
