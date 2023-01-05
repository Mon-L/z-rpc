package cn.zcn.rpc.bootstrap.registry;

import java.util.List;

/**
 * 接口提供者监听，当注册中心的服务提供者列表发生变更时使用该接口更新服务提供者列表。
 *
 * @author zicung
 */
public interface ProviderListener {

    /**
     * 删除一个服务提供者
     *
     * @param provider 待删除的服务提供者
     */
    void removeProvider(Provider provider);

    /**
     * 添加一个服务提供者
     *
     * @param provider 待添加的服务提供者
     */
    void addProvider(Provider provider);

    /**
     * 全量更新服务提供者列表
     *
     * @param newProviders 新的服务提供者列表
     */
    void updateProviders(List<Provider> newProviders);
}
