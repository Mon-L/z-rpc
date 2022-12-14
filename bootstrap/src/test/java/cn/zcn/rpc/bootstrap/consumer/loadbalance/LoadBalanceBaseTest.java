package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.extension.ExtensionLoader;
import cn.zcn.rpc.bootstrap.registry.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;

public class LoadBalanceBaseTest {

    private RpcRequest request;

    @Before
    public void beforeEach() {
        request = new RpcRequest();
        request.setClazz("cn.zcn.rpc.Example");
        request.setMethodName("method");
        request.setParameterTypes(new String[] { int.class.getName(), String.class.getName() });
        request.setParameters(new Object[] { 1, "s" });
    }

    protected Provider createProvider(String ip, int warmup, long startTime, int weight) {
        Provider provider = new Provider();
        provider.setIp(ip);
        provider.setWarmup(warmup);
        provider.setStartTime(startTime);
        provider.setWeight(weight);
        return provider;
    }

    protected Map<Provider, Integer> doSelect(int times, List<Provider> providers, String loadBalance) {
        return doSelectWithRequest(times, providers, loadBalance, request);
    }

    protected Map<Provider, Integer> doSelectWithRequest(int times,
                                                         List<Provider> providers,
                                                         String loadBalance,
                                                         RpcRequest request) {
        Map<Provider, Integer> counter = new HashMap<>();
        LoadBalance lb = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(loadBalance);

        for (int i = 0; i < times; i++) {
            Provider provider = lb.select(providers, request);
            counter.put(provider, counter.getOrDefault(provider, 0) + 1);
        }

        return counter;
    }

    protected long getSum(Map<Provider, Integer> counter) {
        return counter.values().stream().mapToInt(v -> v).sum();
    }
}
