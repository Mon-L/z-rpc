package cn.zcn.rpc.bootstrap.registry;

import cn.zcn.rpc.bootstrap.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 服务提供者集合，同一个 {@link Registry} 获取的服务提供者列表都保存在同一个 {@code ProviderGroup} 中。<p>
 * 实现了 {@code ProviderListener} 接口，可以动态更新服务提供者列表。
 *
 * @author zicung
 */
public class ProviderGroup implements ProviderListener {

    private List<Provider> providers = new CopyOnWriteArrayList<>();

    public List<Provider> getProviders() {
        return Collections.unmodifiableList(new ArrayList<>(providers));
    }

    @Override
    public void removeProvider(Provider provider) {
        providers.remove(provider);
    }

    @Override
    public void addProvider(Provider provider) {
        providers.add(provider);
    }

    @Override
    public void updateProviders(List<Provider> newProviders) {
        if (CollectionUtils.isEmptyOrNull(newProviders)) {
            this.providers.clear();
        } else {
            this.providers = new CopyOnWriteArrayList<>(newProviders);
        }
    }
}
