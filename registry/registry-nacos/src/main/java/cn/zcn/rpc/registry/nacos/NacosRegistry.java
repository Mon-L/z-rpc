package cn.zcn.rpc.registry.nacos;

import cn.zcn.rpc.bootstrap.consumer.ConsumerInterfaceConfig;
import cn.zcn.rpc.bootstrap.extension.Extension;
import cn.zcn.rpc.bootstrap.provider.ProviderConfig;
import cn.zcn.rpc.bootstrap.provider.ProviderInterfaceConfig;
import cn.zcn.rpc.bootstrap.registry.*;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nacos 服务提供者注册中心，提供服务注册与服务订阅功能。
 *
 * <p>
 *
 * <pre>
 * 服务提供者列表在 Nacos 的存储结构：
 *    --| z-rpc (namespace)
 *    ----| cn.zcn.rpc.example.service.PingService:v1.0.0 (interface:version)
 *    ------| cluster1 (cluster)
 *    --------| instances
 *    ----------| {"ip": 10.8.1.1, "port": 3309, "metaData":{......}}
 *    ----------| {"ip": 10.8.1.1, "port": 3309, "metaData":{......}}
 *    ----------| {"ip": 10.8.1.1, "port": 3309, "metaData":{......}}
 *    ----| cn.zcn.rpc.example.service.PongService:v1.0.0
 *    ------| ......
 * </pre>
 *
 * @author zicung
 */
@Extension(NacosRegistry.NACOS)
public class NacosRegistry extends Registry {

    public static final String NACOS = "nacos";

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosRegistry.class);

    private NamingService namingService;
    private final Map<ProviderInterfaceConfig, Instance> instances = new HashMap<>();
    private final Map<ConsumerInterfaceConfig, EventListener> listens = new HashMap<>();

    public NacosRegistry(RegistryConfig registryConfig) {
        super(registryConfig);
    }

    private Properties getProperties(RegistryConfig registryConfig) {
        Properties properties = new Properties();
        String url = registryConfig.getUrl(); // 10.0.1.1:8888, 10.0.1.1:8888/{namespace}

        String namespace, serverAddr;
        int slash = url.indexOf('/');
        if (slash != -1 && slash != url.length() - 1) {
            namespace = url.substring(slash + 1);
            serverAddr = url.substring(0, slash);
        } else {
            namespace = NacosUtils.DEFAULT_NAMESPACE;
            serverAddr = url;
        }

        properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverAddr);

        return properties;
    }

    @Override
    protected void doStart() throws LifecycleException {
        Properties namingProps = getProperties(registryConfig);

        try {
            this.namingService = NamingFactory.createNamingService(namingProps);
        } catch (NacosException e) {
            throw new LifecycleException("Failed to start NacosRegistry.", e);
        }
    }

    @Override
    public void register(ProviderConfig providerConfig, Collection<ProviderInterfaceConfig> providerInterfaceConfigs)
        throws RegistryException {
        try {
            for (ProviderInterfaceConfig interfaceConfig : providerInterfaceConfigs) {
                Instance instance = NacosUtils.toInstance(providerConfig, interfaceConfig);
                namingService.registerInstance(interfaceConfig.getUniqueName(), instance);
                instances.put(interfaceConfig, instance);
            }
        } catch (NacosException e) {
            throw new RegistryException("Error occurred when register instances.", e);
        }
    }

    @Override
    public void unregister(ProviderConfig providerConfig, Collection<ProviderInterfaceConfig> providerInterfaceConfig)
        throws RegistryException {
        try {
            for (ProviderInterfaceConfig itf : providerInterfaceConfig) {
                if (!instances.containsKey(itf)) {
                    LOGGER.warn("Attempt to unregister {} which do not be registered.", itf.toString());
                    continue;
                }

                Instance instance = instances.remove(itf);
                namingService.deregisterInstance(instance.getServiceName(), instance.getClusterName(), instance);
            }
        } catch (NacosException e) {
            throw new RegistryException("Error occurred when unregister instances.", e);
        }
    }

    @Override
    public void subscribe(ConsumerInterfaceConfig consumerInterfaceConfig, ProviderListener providerListener)
        throws RegistryException {
        try {
            EventListener eventListener = event -> {
                if (event instanceof NamingEvent) {
                    NamingEvent namingEvent = (NamingEvent) event;
                    List<Provider> providers = new ArrayList<>(namingEvent.getInstances().size());
                    for (Instance instance : namingEvent.getInstances()) {
                        Provider provider = NacosUtils.toProvider(instance);
                        provider.setService(consumerInterfaceConfig.getInterfaceName());
                        providers.add(provider);
                    }

                    providerListener.updateProviders(providers);
                }
            };

            namingService.subscribe(consumerInterfaceConfig.getUniqueName(), eventListener);
            listens.put(consumerInterfaceConfig, eventListener);
        } catch (NacosException e) {
            throw new RegistryException(
                "Error occurred when subscribe instances. Interface:{}",
                consumerInterfaceConfig.getUniqueName(),
                e);
        }
    }

    @Override
    public void unsubscribe(ConsumerInterfaceConfig consumerInterfaceConfig) throws RegistryException {
        if (!listens.containsKey(consumerInterfaceConfig)) {
            LOGGER.warn("Attempt to unsubscribe {} which do not subscribe.", consumerInterfaceConfig.toString());
            return;
        }

        try {
            EventListener eventListener = listens.remove(consumerInterfaceConfig);
            namingService.unsubscribe(consumerInterfaceConfig.getUniqueName(), eventListener);
        } catch (NacosException e) {
            throw new RegistryException(
                "Error occurred when unsubscribe instances. Interface:{}",
                consumerInterfaceConfig.getUniqueName(),
                e);
        }
    }

    @Override
    public List<Provider> loadProviders(ConsumerInterfaceConfig consumerInterfaceConfig) throws RegistryException {
        try {
            List<Instance> instances = namingService.getAllInstances(consumerInterfaceConfig.getUniqueName());
            List<Provider> providers = new ArrayList<>(instances.size());
            for (Instance instance : instances) {
                Provider provider = new Provider();
                provider.setIp(instance.getIp());
                provider.setPort(instance.getPort());

                providers.add(provider);
            }
            return providers;
        } catch (NacosException e) {
            throw new RegistryException("Error occurred when load provider. Interface:{}",
                consumerInterfaceConfig.getUniqueName(), e);
        }
    }

    @Override
    protected void doStop() throws LifecycleException {
        for (ConsumerInterfaceConfig itf : listens.keySet()) {
            unsubscribe(itf);
        }
    }
}
