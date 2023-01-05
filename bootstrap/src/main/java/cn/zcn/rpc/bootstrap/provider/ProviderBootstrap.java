package cn.zcn.rpc.bootstrap.provider;

import cn.zcn.rpc.bootstrap.registry.Registry;
import cn.zcn.rpc.bootstrap.registry.RegistryConfig;
import cn.zcn.rpc.bootstrap.registry.RegistryFactory;
import cn.zcn.rpc.bootstrap.utils.NetUtils;
import cn.zcn.rpc.bootstrap.utils.StringUtils;
import cn.zcn.rpc.remoting.RemotingServer;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * RPC 服务端引导器。</p>
 * 根据 {@code ProviderConfig} 配置信息，创建 RPC 服务。
 *
 * @author zicung
 */
public class ProviderBootstrap extends AbstractLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderBootstrap.class);

    private final ProviderConfig providerConfig;

    private final Set<Registry> registries = new HashSet<>();

    private RemotingServer remotingServer;

    public ProviderBootstrap(ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }

    @Override
    protected void doStart() throws LifecycleException {
        checkConfig();

        //init request handler
        ProviderRequestHandler providerRequestHandler = new ProviderRequestHandler(providerConfig, ProviderBootstrap.this::isStarted);
        providerRequestHandler.resolve();

        this.remotingServer = new RemotingServer(providerConfig.getHost(), providerConfig.getPort());
        configRemotingServer(remotingServer);

        //register rpc handler
        remotingServer.registerRequestHandler(providerRequestHandler);

        //start up port
        remotingServer.start();

        //register interfaces
        register();
    }

    protected void configRemotingServer(RemotingServer remotingServer) {
        //do nothing
    }

    private void checkConfig() {
        if (StringUtils.isEmptyOrNull(providerConfig.getHost())) {
            providerConfig.host(NetUtils.getLocalHost());
            LOGGER.warn("No host is specified. Auto assign host: {}", providerConfig.getHost());
        }

        if (providerConfig.getPort() == 0) {
            providerConfig.port(NetUtils.getAvailablePort());
            LOGGER.warn("No port is specified. Auto assign port: {}", providerConfig.getPort());
        }
    }

    /**
     * 将接口信息注册到注册中心
     */
    private void register() {
        for (RegistryConfig registryConfig : providerConfig.getRegistryConfigs()) {
            Registry registry = RegistryFactory.get().getOrCreateRegistry(registryConfig);
            registry.retain();
            registries.add(registry);

            try {
                registry.register(providerConfig, providerConfig.getInterfaceConfigs());
            } catch (Throwable t) {
                LOGGER.warn("Failed to register provider interfaces. Registry type: {}, Registry url: {}",
                        registryConfig.getType(), registryConfig.getUrl(), t);
            }
        }
    }

    @Override
    protected void doStop() throws LifecycleException {
        for (Registry registry : registries) {
            try {
                registry.unregister(providerConfig.getInterfaceConfigs());
            } catch (Throwable t) {
                LOGGER.warn("Failed to unregister provider interfaces.Registry:{}", registry.toString(), t);
            }
            registry.release();
        }

        remotingServer.stop();
    }
}
