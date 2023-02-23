package cn.zcn.rpc.bootstrap.provider;

import cn.zcn.rpc.bootstrap.registry.Registry;
import cn.zcn.rpc.bootstrap.registry.RegistryConfig;
import cn.zcn.rpc.bootstrap.registry.RegistryFactory;
import cn.zcn.rpc.bootstrap.utils.NetUtils;
import cn.zcn.rpc.bootstrap.utils.StringUtils;
import cn.zcn.rpc.remoting.RemotingServer;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC 服务端引导器。 根据 {@code ProviderConfig} 配置信息，创建 RPC 服务。
 *
 * @author zicung
 */
public class ProviderBootstrap extends AbstractLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderBootstrap.class);

    private final ProviderConfig providerConfig;

    private final Set<Registry> registries = new HashSet<>();

    private final Set<ProviderInterfaceConfig> interfaceConfigs = new HashSet<>();

    private RemotingServer remotingServer;

    private ProviderRequestHandler providerRequestHandler;

    public ProviderBootstrap(ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    @Override
    protected void doStart() throws LifecycleException {
        checkConfig();

        // init request handler
        this.providerRequestHandler = new ProviderRequestHandler(providerConfig,
            ProviderBootstrap.this::isStarted);

        this.remotingServer = new RemotingServer(providerConfig.getHost(), providerConfig.getPort());

        // register rpc handler
        remotingServer.registerRequestHandler(providerRequestHandler);

        // init register
        initRegister();

        // register interfaces
        for (ProviderInterfaceConfig interfaceConfig : interfaceConfigs) {
            registerInterface(interfaceConfig);
        }

        // start up port
        remotingServer.start();
    }

    /**
     * 添加 RPC 接口，使用该方法只能在服务未启动前调用。服务启动时会注册接口到注册中心。
     */
    public void addInterface(ProviderInterfaceConfig interfaceConfig) {
        if (isStarted()) {
            throw new IllegalStateException(
                "ProviderBootstrap is started, should use registerInterface() to registry providerInterfaceConfig.");
        }

        if (interfaceConfigs.contains(interfaceConfig)) {
            throw new IllegalArgumentException(
                "Do not add providerInterfaceConfig repeatedly, " + interfaceConfig.getUniqueName());
        }

        interfaceConfigs.add(interfaceConfig);
    }

    /**
     * 注册 RPC 接口，使用该方法只能在服务启动后调用。
     */
    public void registerInterface(ProviderInterfaceConfig interfaceConfig) {
        if (!isStarted()) {
            throw new IllegalStateException(
                "ProviderBootstrap is not started, should use addInterface() to add providerInterfaceConfig.");
        }

        providerRequestHandler.addInterface(interfaceConfig);

        for (Registry registry : registries) {
            try {
                registry.register(providerConfig, Collections.singletonList(interfaceConfig));
            } catch (Throwable t) {
                LOGGER.warn(
                    "Failed to register provider interfaces. Registry : {}",
                    registry.toString(),
                    t);
            }
        }
    }

    private void checkConfig() {
        if (StringUtils.isEmptyOrNull(providerConfig.getHost())) {
            providerConfig.setHost(NetUtils.getLocalHost());
            LOGGER.warn("No host is specified. Auto assign host: {}", providerConfig.getHost());
        }

        if (providerConfig.getPort() == 0) {
            providerConfig.setPort(NetUtils.getAvailablePort());
            LOGGER.warn("No port is specified. Auto assign port: {}", providerConfig.getPort());
        }
    }

    /**
     * 初始化注册中心
     */
    private void initRegister() {
        for (RegistryConfig registryConfig : providerConfig.getRegistryConfigs()) {
            Registry registry = RegistryFactory.get().getOrCreateRegistry(registryConfig);
            registry.retain();
            registries.add(registry);
        }
    }

    @Override
    protected void doStop() throws LifecycleException {
        for (Registry registry : registries) {
            try {
                registry.unregister(providerConfig, interfaceConfigs);
            } catch (Throwable t) {
                LOGGER.warn("Failed to unregister provider interfaces.Registry:{}", registry.toString(), t);
            }
            registry.release();
        }

        remotingServer.stop();
    }
}
