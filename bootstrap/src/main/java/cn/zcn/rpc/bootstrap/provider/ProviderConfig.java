package cn.zcn.rpc.bootstrap.provider;

import cn.zcn.rpc.bootstrap.RpcConfigs;
import cn.zcn.rpc.bootstrap.registry.RegistryConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * 服务提供者配置
 */
public class ProviderConfig {

    /**
     * 名称
     */
    private String name;

    /**
     * ip
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * 服务权重
     */
    private int weight = RpcConfigs.getInteger(RpcConfigs.WEIGHT, 5);

    /**
     * 服务预热时间。默认十五分钟。
     */
    private int warmup = RpcConfigs.getInteger(RpcConfigs.WARMUP, 900000);

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
        if (port <= 0) {
            throw new IllegalArgumentException("Provider port must not be negative.");
        }

        this.port = port;
        return this;
    }

    public int getWeight() {
        return weight;
    }

    public ProviderConfig weight(int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Provider weight must not be negative.");
        }

        this.weight = weight;
        return this;
    }

    public int getWarmup() {
        return warmup;
    }

    public ProviderConfig setWarmup(int warmup) {
        if (warmup < 0) {
            throw new IllegalArgumentException("Provider warmup must not be negative.");
        }

        this.warmup = warmup;
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
