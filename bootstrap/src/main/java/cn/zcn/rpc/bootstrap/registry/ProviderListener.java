package cn.zcn.rpc.bootstrap.registry;

import java.util.List;

public interface ProviderListener {
    void removeProvider(Provider provider);

    void addProvider(Provider provider);

    void updateProviders(List<Provider> newProviders);
}
