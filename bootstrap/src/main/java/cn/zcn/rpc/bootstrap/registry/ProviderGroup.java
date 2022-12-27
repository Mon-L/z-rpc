package cn.zcn.rpc.bootstrap.registry;

import cn.zcn.rpc.bootstrap.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
