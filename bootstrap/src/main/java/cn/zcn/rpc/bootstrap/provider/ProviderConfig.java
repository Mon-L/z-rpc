package cn.zcn.rpc.bootstrap.provider;

import cn.zcn.rpc.bootstrap.RpcConfigs;
import cn.zcn.rpc.bootstrap.registry.RegistryConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * 服务提供者配置
 */
public class ProviderConfig {

    private String name;

    private String host;

    private int port;

    private boolean ignoreTimeoutRequest = RpcConfigs.getBool(RpcConfigs.IGNORE_TIMEOUT_REQUEST, false);

    private final Set<ProviderInterfaceConfig> interfaceConfigs = new HashSet<>();

    private final Set<RegistryConfig> registryConfigs = new HashSet<>();

    public String getName() {
        return name;
    }

    public ProviderConfig name(String name) {
        this.name = name;
        return this;
    }

    public String getHost() {
        return host;
    }

    public ProviderConfig host(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ProviderConfig port(int port) {
        this.port = port;
        return this;
    }

    public boolean isIgnoreTimeoutRequest() {
        return ignoreTimeoutRequest;
    }

    public ProviderConfig ignoreTimeoutRequest(boolean ignoreTimeoutRequest) {
        this.ignoreTimeoutRequest = ignoreTimeoutRequest;
        return this;
    }

    public ProviderConfig addInterfaceConfig(ProviderInterfaceConfig providerInterfaceConfig) {
        interfaceConfigs.add(providerInterfaceConfig);
        return this;
    }

    public Set<ProviderInterfaceConfig> getInterfaceConfigs() {
        return interfaceConfigs;
    }

    public ProviderConfig addRegistryConfig(RegistryConfig registry) {
        registryConfigs.add(registry);
        return this;
    }

    public Set<RegistryConfig> getRegistryConfigs() {
        return registryConfigs;
    }
}
