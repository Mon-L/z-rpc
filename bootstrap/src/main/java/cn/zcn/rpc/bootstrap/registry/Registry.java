package cn.zcn.rpc.bootstrap.registry;

import cn.zcn.rpc.bootstrap.consumer.ConsumerInterfaceConfig;
import cn.zcn.rpc.bootstrap.extension.ExtensionPoint;
import cn.zcn.rpc.bootstrap.provider.ProviderConfig;
import cn.zcn.rpc.bootstrap.provider.ProviderInterfaceConfig;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ExtensionPoint
public abstract class Registry extends AbstractLifecycle {

    /**
     * 引用计数器
     */
    private final AtomicInteger refCnt = new AtomicInteger(0);

    protected final RegistryConfig registryConfig;

    public Registry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    /**
     * 引用次数增加 1
     */
    public void retain() {
        refCnt.incrementAndGet();
    }

    /**
     * 引用次数减少 1。当引用次数为 0 时调用 {@link Registry#stop()}
     */
    public void release() {
        if (refCnt.decrementAndGet() == 0) {
            stop();
        }
    }

    public abstract void register(ProviderConfig providerConfig, Collection<ProviderInterfaceConfig> providerInterfaceConfig) throws RegistryException;

    public abstract void unregister(Collection<ProviderInterfaceConfig> providerInterfaceConfig) throws RegistryException;

    public abstract List<Provider> loadProviders(ConsumerInterfaceConfig consumerInterfaceConfig) throws RegistryException;

    public abstract void subscribe(ConsumerInterfaceConfig consumerInterfaceConfig, ProviderListener providerListener) throws RegistryException;

    public abstract void unsubscribe(ConsumerInterfaceConfig consumerInterfaceConfig) throws RegistryException;

    @Override
    public void stop() throws LifecycleException {
        if (refCnt.get() > 0) {
            throw new LifecycleException("Reference count is greater than 1. Should not stop registry.");
        }
        super.stop();
    }

    @Override
    public String toString() {
        return "Registry{" +
                "type=" + registryConfig.getType() +
                ", url=" + registryConfig.getUrl() +
                '}';
    }
}
