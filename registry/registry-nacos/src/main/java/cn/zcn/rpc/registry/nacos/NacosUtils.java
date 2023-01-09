package cn.zcn.rpc.registry.nacos;

import cn.zcn.rpc.bootstrap.provider.ProviderConfig;
import cn.zcn.rpc.bootstrap.provider.ProviderInterfaceConfig;
import cn.zcn.rpc.bootstrap.registry.Provider;
import com.alibaba.nacos.api.naming.pojo.Instance;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/** @author zicung */
public class NacosUtils {

    protected static final String WEIGHT = "weight";
    protected static final String WARMUP = "warmup";
    protected static final String START_TIME = "startTime";
    protected static final String DEFAULT_CLUSTER = "DEFAULT-CLUSTER";
    protected static final String DEFAULT_NAMESPACE = "z-rpc";

    protected static Instance toInstance(ProviderConfig providerConfig, ProviderInterfaceConfig interfaceConfig) {
        Instance instance = new Instance();

        instance.setServiceName(interfaceConfig.getUniqueName());
        instance.setClusterName(DEFAULT_CLUSTER);
        instance.setIp(providerConfig.getHost());
        instance.setPort(providerConfig.getPort());

        Map<String, String> metadata = new HashMap<>();
        metadata.put(WEIGHT, String.valueOf(providerConfig.getWeight()));
        metadata.put(WARMUP, String.valueOf(providerConfig.getWarmup()));
        metadata.put(
            START_TIME, String.valueOf(ManagementFactory.getRuntimeMXBean().getStartTime()));
        instance.setMetadata(metadata);

        return instance;
    }

    protected static Provider toProvider(Instance instance) {
        Provider provider = new Provider();
        provider.setIp(instance.getIp());
        provider.setPort(instance.getPort());

        Map<String, String> metadata = instance.getMetadata();
        provider.setWeight(Integer.parseInt(metadata.get(WEIGHT)));
        provider.setWarmup(Integer.parseInt(metadata.get(WARMUP)));
        provider.setStartTime(Long.parseLong(metadata.get(START_TIME)));

        return provider;
    }
}
