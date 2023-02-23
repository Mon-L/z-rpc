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

/**
 * 注册中心。
 *
 * <pre>
 * 地址相同的 {@code Registry} 在同一个进程中只会有一个实例。{@code Registry} 有一个引用计数器{@code refCnt}，
 * 当 {@code Registry} 被服务提供者或服务消费者引用时，调用 {@code retain()} 使 {@code refCnt} 加一，释放时调用
 * {@code release()}。只有当所有服务提供者和服务消费者都释放 {@code Registry} 的实例后，{@code Registry} 才可以被关闭。
 * </pre>
 *
 * @author zicung
 */
@ExtensionPoint
public abstract class Registry extends AbstractLifecycle {

    /** 引用计数器 */
    private final AtomicInteger refCnt = new AtomicInteger(0);

    protected final RegistryConfig registryConfig;

    public Registry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    /** 引用次数增加 1 */
    public void retain() {
        refCnt.incrementAndGet();
    }

    /** 引用次数减少 1。当引用次数为 0 时调用 {@code stop()} 关闭注册中心。 */
    public void release() {
        if (refCnt.decrementAndGet() == 0) {
            stop();
        }
    }

    /**
     * 批量注册接口提供者
     *
     * @param providerConfig 服务提供者
     * @param providerInterfaceConfigs 待注册的接口
     * @throws RegistryException 失败异常
     */
    public abstract void register(ProviderConfig providerConfig,
                                  Collection<ProviderInterfaceConfig> providerInterfaceConfigs)
        throws RegistryException;

    /**
     * 批量注销接口提供者
     *
     * @param providerConfig 服务提供者
     * @param providerInterfaceConfigs 待注销的接口
     * @throws RegistryException 失败异常
     */
    public abstract void unregister(ProviderConfig providerConfig,
                                    Collection<ProviderInterfaceConfig> providerInterfaceConfigs)
        throws RegistryException;

    /**
     * 获取指定接口的提供者列表
     *
     * @param consumerInterfaceConfig 接口
     * @return 提供者列表
     * @throws RegistryException 失败异常
     */
    public abstract List<Provider> loadProviders(ConsumerInterfaceConfig consumerInterfaceConfig)
        throws RegistryException;

    /**
     * 订阅指定接口的提供者信息
     *
     * @param consumerInterfaceConfig 待订阅的接口
     * @param providerListener 订阅监听，当服务提供者列表发生改变时会调用此监听
     * @throws RegistryException 失败异常
     */
    public abstract void subscribe(ConsumerInterfaceConfig consumerInterfaceConfig, ProviderListener providerListener)
        throws RegistryException;

    /**
     * 取消订阅
     *
     * @param consumerInterfaceConfig 待取消订阅的接口
     * @throws RegistryException 失败异常
     */
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
        return "Registry{" + "type=" + registryConfig.getType() + ", url=" + registryConfig.getUrl() + '}';
    }
}
