package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.InterfaceConfig;
import cn.zcn.rpc.bootstrap.InvokeType;
import cn.zcn.rpc.bootstrap.RpcConfigs;
import cn.zcn.rpc.bootstrap.registry.RegistryConfig;
import java.util.List;
import java.util.Set;

/**
 * 服务消费者接口配置
 *
 * @author zicung
 */
public class ConsumerInterfaceConfig extends InterfaceConfig {

    /** 服务提供者地址。不通过注册中心获取服务提供者地址。 */
    private String providerUrl;

    /** 注册中心地址 */
    private Set<RegistryConfig> registryConfigs;

    /** 与每个服务提供者的最大连接数 */
    private int maxConnectionPerUrl = RpcConfigs.getInteger(RpcConfigs.MAX_CONNECTION_PER_URL, 1);

    /** 代理方式 */
    private String proxy = RpcConfigs.getString(RpcConfigs.PROXY, "jdk");

    /** 负载均衡器 */
    private String loadBalance = RpcConfigs.getString(RpcConfigs.LOAD_BALANCE, "random");

    /** 请求超时时间，单位毫秒 */
    private int timeout = RpcConfigs.getInteger(RpcConfigs.TIMEOUT, 30000);

    /** 过滤器 */
    private List<String> filters = RpcConfigs.getList(RpcConfigs.FILTERS);

    /** 调用类型 */
    private InvokeType invokeType = InvokeType.SYNC;

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

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public InvokeType getInvokeType() {
        return invokeType;
    }

    public void setInvokeType(InvokeType invokeType) {
        this.invokeType = invokeType;
    }

    public int getMaxConnectionPerUrl() {
        return maxConnectionPerUrl;
    }

    public void setMaxConnectionPerUrl(int maxConnectionPerUrl) {
        this.maxConnectionPerUrl = maxConnectionPerUrl;
    }
}
