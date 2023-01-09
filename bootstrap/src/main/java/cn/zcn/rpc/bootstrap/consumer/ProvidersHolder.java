package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.registry.Provider;
import cn.zcn.rpc.bootstrap.registry.ProviderGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 管理接口的所有 {@code ProviderGroup}。
 *
 * @author zicung
 */
public class ProvidersHolder {

    private final List<ProviderGroup> groups = new CopyOnWriteArrayList<>();

    public List<Provider> getProviders() {
        List<Provider> providers = new ArrayList<>();
        int i = 0, size = groups.size();
        while (i < size) {
            providers.addAll(groups.get(i).getProviders());
            i++;
        }

        return providers;
    }

    public void addProviderGroup(ProviderGroup group) {
        groups.add(group);
    }
}
