package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.consumer.proxy.Proxy;
import cn.zcn.rpc.bootstrap.extension.ExtensionLoader;
import cn.zcn.rpc.bootstrap.registry.*;
import cn.zcn.rpc.bootstrap.utils.CollectionUtils;
import cn.zcn.rpc.bootstrap.utils.StringUtils;
import cn.zcn.rpc.remoting.RemotingClient;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            Provider directProvider = new Provider();
            directProvider.setIp(interfaceConfig.getProviderUrl());

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
                    LOGGER.warn("Failed to load providerInfo. Interface:{}, Registry:{}", interfaceConfig.getUniqueName(),
                            registryConfig.toString());
                }

                try {
                    registry.subscribe(interfaceConfig, providerGroup);
                } catch (Throwable t) {
                    LOGGER.warn("Failed to subscribe providerInfo. Interface:{}, Registry:{}", interfaceConfig.getUniqueName(),
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

    public Object createProxy() {
        DefaultInvoker invoker = new DefaultInvoker(interfaceConfig, remotingClient, providersHolder);
        invoker.init();

        Proxy proxy = ExtensionLoader.getExtensionLoader(Proxy.class).getExtension(interfaceConfig.getProxy());
        return proxy.createProxy(interfaceConfig.getInterfaceClass(), invoker);
    }

    @Override
    protected void doStop() throws LifecycleException {
        for (Registry registry : registries) {
            try {
                registry.unsubscribe(interfaceConfig);
            } catch (Throwable t) {
                LOGGER.warn("Failed to unsubscribe providerInfo. Interface:{}, Registry:{}", interfaceConfig.getUniqueName(),
                        registry.toString());
            }

            registry.release();
        }
    }

    @Override
    public String toString() {
        return "InterfaceBootstrap{" +
                "interfaceConfig=" + interfaceConfig.getUniqueName() +
                '}';
    }
}
