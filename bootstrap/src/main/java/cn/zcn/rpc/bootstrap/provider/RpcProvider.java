package cn.zcn.rpc.bootstrap.provider;

import cn.zcn.rpc.bootstrap.RegistryConfig;
import cn.zcn.rpc.bootstrap.extension.ExtensionLoader;
import cn.zcn.rpc.bootstrap.registry.Registry;
import cn.zcn.rpc.remoting.RemotingServer;
import cn.zcn.rpc.remoting.config.Option;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class RpcProvider extends AbstractLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProvider.class);

    private final ProviderOptions options;
    private final RemotingServer remotingServer;
    private final Set<InterfaceConfig> interfaceConfigs = new HashSet<>();
    private final Set<RegistryConfig> registryConfigs = new HashSet<>();
    private final Set<Registry> registries = new HashSet<>();

    public RpcProvider(String ip, int port) {
        this.options = new ProviderOptions();
        this.remotingServer = new RemotingServer(ip, port);
    }

    public <T> void option(Option<T> option, T value) {
        options.setOption(option, value);
    }

    @Override
    protected void doStart() throws LifecycleException {
        ProviderRequestHandler providerRequestHandler = new ProviderRequestHandler(RpcProvider.this::isStarted);

        //resolve interfaces
        providerRequestHandler.resolve(interfaceConfigs);

        //register rpc handler
        remotingServer.registerRequestHandler(providerRequestHandler);

        //start up port
        remotingServer.start();

        //register interfaces
        registerInterfaces();
    }

    /**
     * 将接口信息注册到注册中心
     */
    private void registerInterfaces() {
        for (RegistryConfig config : registryConfigs) {
            Registry registry = ExtensionLoader.getExtensionLoader(Registry.class).getExtension(config.getType());
            registry.init(config);
            registry.start();

            try {
                registry.register(interfaceConfigs);
            } catch (Throwable t) {
                LOGGER.warn("Failed to registry provider interfaces. Registry type:{}, Registry url:{}", config.getType(), config.getUrl(), t);
            }
            registries.add(registry);
        }
    }

    public RpcProvider addInterface(InterfaceConfig interfaceConfig) {
        if (isStarted()) {
            throw new IllegalStateException("RpcProvider was started. Should add interfaceConfig before start.");
        }

        interfaceConfigs.add(interfaceConfig);
        return this;
    }

    public RpcProvider addRegistry(RegistryConfig registry) {
        if (isStarted()) {
            throw new IllegalStateException("RpcProvider was started. Should add registryConfig before start.");
        }

        registryConfigs.add(registry);
        return this;
    }

    @Override
    protected void doStop() throws LifecycleException {
        for (Registry registry : registries) {
            try {
                registry.unregister(interfaceConfigs);
            } catch (Throwable t) {
                LOGGER.warn("Failed to unregister provider interfaces.Registry:{}", registry.toString(), t);
            }

            registry.stop();
        }

        remotingServer.stop();
    }
}
