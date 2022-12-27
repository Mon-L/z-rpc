package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.registry.Provider;
import cn.zcn.rpc.bootstrap.registry.ProviderGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProvidersHolder {

    private final List<ProviderGroup> groups = new CopyOnWriteArrayList<>();

    public List<Provider> getProviderInfos() {
        List<Provider> providers = new ArrayList<>();
        int size = groups.size();
        for (int i = 0; i < size; i++) {
            providers.addAll(groups.get(i).getProviders());
        }

        return providers;
    }

    public void addProviderGroup(ProviderGroup group) {
        groups.add(group);
    }
}
