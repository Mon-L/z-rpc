package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.InterfaceConfig;
import cn.zcn.rpc.bootstrap.RpcConfigs;
import cn.zcn.rpc.bootstrap.registry.RegistryConfig;

import java.util.Set;

/**
 * 服务消费者接口配置
 */
public class ConsumerInterfaceConfig extends InterfaceConfig {

    /**
     * 服务提供者地址。不通过注册中心获取服务提供者地址。
     */
    private String providerUrl;

    /**
     * 注册中心地址
     */
    private Set<RegistryConfig> registryConfigs;

    /**
     * 代理方式
     */
    private String proxy = RpcConfigs.getString(RpcConfigs.PROXY, "jdk");

    /**
     * 路由器
     */
    private String router = RpcConfigs.getString(RpcConfigs.ROUTER, "default");

    /**
     * 负载均衡器
     */
    private String loadBalancer = RpcConfigs.getString(RpcConfigs.LOADBALANCER, "random");

    /**
     * 请求超时时间，单位秒
     */
    private int timeout = RpcConfigs.getInteger(RpcConfigs.TIMEOUT, 30);

    public ConsumerInterfaceConfig(Class<?> clazz) {
        super(clazz);
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    public Set<RegistryConfig> getRegistryConfigs() {
        return registryConfigs;
    }

    public void setRegistryConfigs(Set<RegistryConfig> registryConfigs) {
        this.registryConfigs = registryConfigs;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getRouter() {
        return router;
    }

    public void setRouter(String router) {
        this.router = router;
    }

    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }
}
