package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.consumer.proxy.Proxy;
import cn.zcn.rpc.bootstrap.extension.ExtensionLoader;
import cn.zcn.rpc.bootstrap.registry.*;
import cn.zcn.rpc.bootstrap.utils.CollectionUtils;
import cn.zcn.rpc.bootstrap.utils.StringUtils;
import cn.zcn.rpc.remoting.RemotingClient;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务消费者的接口代理引导器，订阅接口的服务提供者列表、创建接口代理。
 *
 * @author zicung 
 */
public class InterfaceBootstrap extends AbstractLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceBootstrap.class);

    private final RemotingClient remotingClient;
    private final ConsumerInterfaceConfig interfaceConfig;
    private final List<Registry> registries = new ArrayList<>();
    private ProvidersHolder providersHolder;

    public InterfaceBootstrap(ConsumerInterfaceConfig interfaceConfig, RemotingClient remotingClient) {
        this.interfaceConfig = interfaceConfig;
        this.remotingClient = remotingClient;
    }

    @Override
    protected void doStart() throws LifecycleException {
        this.providersHolder = resolveRegistry();
    }

    private ProvidersHolder resolveRegistry() {
        ProvidersHolder providersHolder = new ProvidersHolder();

        if (!StringUtils.isEmptyOrNull(interfaceConfig.getProviderUrl())) {
            Provider directProvider = urlToProvider(interfaceConfig.getProviderUrl());

            ProviderGroup directProviderGroup = new ProviderGroup();
            directProviderGroup.updateProviders(Collections.singletonList(directProvider));
            providersHolder.addProviderGroup(directProviderGroup);
        } else if (!CollectionUtils.isEmptyOrNull(interfaceConfig.getRegistryConfigs())) {
            for (RegistryConfig registryConfig : interfaceConfig.getRegistryConfigs()) {
                Registry registry = RegistryFactory.get().getOrCreateRegistry(registryConfig);
                registry.retain();

                ProviderGroup providerGroup = new ProviderGroup();
                try {
                    providerGroup.updateProviders(registry.loadProviders(interfaceConfig));
                } catch (Throwable t) {
                    LOGGER.warn(
                        "Failed to load providerInfo. Interface:{}, Registry:{}",
                        interfaceConfig.getUniqueName(),
                        registryConfig.toString());
                }

                try {
                    registry.subscribe(interfaceConfig, providerGroup);
                } catch (Throwable t) {
                    LOGGER.warn(
                        "Failed to subscribe providerInfo. Interface:{}, Registry:{}",
                        interfaceConfig.getUniqueName(),
                        registryConfig.toString());
                }

                providersHolder.addProviderGroup(providerGroup);
                registries.add(registry);
            }
        } else {
            throw new LifecycleException("ConsumerInterfaceConfig must contain registerConfigs or providerUrl.");
        }

        return providersHolder;
    }

    private Provider urlToProvider(String url) {
        int colon = url.indexOf(':');

        if (colon != -1) {
            Provider provider = new Provider();
            provider.setIp(url.substring(0, colon));
            provider.setPort(Integer.parseInt(url.substring(colon + 1)));
            return provider;
        }

        throw new IllegalArgumentException("Invalid provider url:" + url);
    }

    public Object createProxy() {
        DefaultRpcInvoker rpcInvoker = new DefaultRpcInvoker(interfaceConfig, remotingClient, providersHolder);
        rpcInvoker.init();

        Proxy proxy = ExtensionLoader.getExtensionLoader(Proxy.class).getExtension(interfaceConfig.getProxy());
        return proxy.createProxy(interfaceConfig.getInterfaceClass(), rpcInvoker);
    }

    @Override
    protected void doStop() throws LifecycleException {
        for (Registry registry : registries) {
            try {
                registry.unsubscribe(interfaceConfig);
            } catch (Throwable t) {
                LOGGER.warn(
                    "Failed to unsubscribe providerInfo. Interface:{}, Registry:{}",
                    interfaceConfig.getUniqueName(),
                    registry.toString());
            }

            registry.release();
        }
    }

    @Override
    public String toString() {
        return "InterfaceBootstrap{" + "interfaceConfig=" + interfaceConfig.getUniqueName() + '}';
    }
}
