package cn.zcn.rpc.registry.zookeeper;

import cn.zcn.rpc.bootstrap.consumer.ConsumerInterfaceConfig;
import cn.zcn.rpc.bootstrap.extension.Extension;
import cn.zcn.rpc.bootstrap.provider.ProviderConfig;
import cn.zcn.rpc.bootstrap.provider.ProviderInterfaceConfig;
import cn.zcn.rpc.bootstrap.registry.*;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zookeeper 注册中心，提供服务注册与服务订阅功能。
 * <p>
 * <pre>
 * 服务提供者在 Zookeeper 的存储结构：
 * --| z-rpc
 * ----| cn.zcn.rpc.example.service.PingService:v1.0.0 (interface:version)
 * ------| providers
 * --------| 10.8.1.1:3309/cn.zcn.rpc.example.service.PingService:v1.0.0?weight=3
 * --------| 10.8.1.2:4309/cn.zcn.rpc.example.service.PingService:v1.0.0?weight=3
 *
 * </pre>
 * @author zicung
 */
@Extension(ZookeeperRegistry.ZOOKEEPER)
public class ZookeeperRegistry extends Registry {
    public static final String ZOOKEEPER = "zookeeper";

    static final String ROOT = "z-rpc";
    static final String PROVIDERS = "providers";

    private static final String CHARSET = "UTF-8";
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private CuratorFramework client;

    private final Map<ConsumerInterfaceConfig, PathChildrenCache> consumerPathChildrenCaches = new ConcurrentHashMap<>();

    public ZookeeperRegistry(RegistryConfig registryConfig) {
        super(registryConfig);
    }

    @Override
    protected void doStart() throws LifecycleException {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(registryConfig.getUrl(), retryPolicy);
        client.start();
    }

    private CuratorFramework getAndCheckCurator() {
        if (client == null || client.getState() != CuratorFrameworkState.STARTED) {
            throw new RegistryException("Zookeeper client is unavailable.");
        }

        return client;
    }

    @Override
    public void register(ProviderConfig providerConfig, Collection<ProviderInterfaceConfig> providerInterfaceConfigs)
        throws RegistryException {

        String providerUrl = null;
        try {
            for (ProviderInterfaceConfig interfaceConfig : providerInterfaceConfigs) {
                providerUrl = getProviderUrl(providerConfig, interfaceConfig);

                getAndCheckCurator()
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(makeProviderPath(interfaceConfig.getUniqueName(), providerUrl));
            }
        } catch (KeeperException.NodeExistsException e) {
            LOGGER.warn("Provider has exists in zookeeper. Provider:{}", providerUrl);
        } catch (Exception e) {
            throw new RegistryException("Error occurred when register provider instances.", e);
        }
    }

    @Override
    public void unregister(ProviderConfig providerConfig, Collection<ProviderInterfaceConfig> providerInterfaceConfigs)
        throws RegistryException {
        try {
            for (ProviderInterfaceConfig interfaceConfig : providerInterfaceConfigs) {
                String providerUrl = getProviderUrl(providerConfig, interfaceConfig);
                getAndCheckCurator()
                    .delete()
                    .forPath(makeProviderPath(interfaceConfig.getUniqueName(), providerUrl));
            }
        } catch (Exception e) {
            throw new RegistryException("Error occurred when unregister provider instances.", e);
        }
    }

    @Override
    public List<Provider> loadProviders(ConsumerInterfaceConfig consumerInterfaceConfig) throws RegistryException {
        List<Provider> providers = new ArrayList<>();

        try {
            List<String> providerNodes = getAndCheckCurator()
                .getChildren()
                .forPath(makeProviderPath(consumerInterfaceConfig.getInterfaceName()));

            if (!providerNodes.isEmpty()) {
                for (String node : providerNodes) {
                    providers.add(parseProvider(node));
                }
            }
        } catch (KeeperException.NoNodeException ignored) {
            //do nothing
        } catch (Exception e) {
            throw new RegistryException("Error occurred when load provider instances. Interface:{}",
                consumerInterfaceConfig.getUniqueName(), e);
        }

        return providers;
    }

    @Override
    public void subscribe(ConsumerInterfaceConfig consumerInterfaceConfig, ProviderListener providerListener)
        throws RegistryException {
        String subscribePath = makeProviderPath(consumerInterfaceConfig.getUniqueName());
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, subscribePath, true);
        pathChildrenCache.getListenable().addListener((curatorFramework, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    providerListener.addProvider(parseProviderByPath(event.getData().getPath()));
                    break;
                case CHILD_REMOVED:
                    providerListener.removeProvider(parseProviderByPath(event.getData().getPath()));
                    break;
                default:
                    break;
            }
        });

        try {
            pathChildrenCache.start();
            consumerPathChildrenCaches.put(consumerInterfaceConfig, pathChildrenCache);
        } catch (Exception e) {
            throw new RegistryException("Error occurred when subscribe interface. Interface:{}",
                consumerInterfaceConfig.getUniqueName(), e);
        }
    }

    @Override
    public void unsubscribe(ConsumerInterfaceConfig consumerInterfaceConfig) throws RegistryException {
        PathChildrenCache pathChildrenCache = consumerPathChildrenCaches.get(consumerInterfaceConfig);
        if (pathChildrenCache != null) {
            try {
                pathChildrenCache.close();
            } catch (IOException e) {
                throw new RegistryException("Error occurred when unsubscribe interface. Interface:{}",
                    consumerInterfaceConfig.getUniqueName(), e);
            }
        }
    }

    @Override
    protected void doStop() throws LifecycleException {
        try {
            //移除订阅
            for (Map.Entry<ConsumerInterfaceConfig, PathChildrenCache> entry : consumerPathChildrenCaches.entrySet()) {
                entry.getValue().close();
                consumerPathChildrenCaches.remove(entry.getKey());
            }
        } catch (IOException e) {
            throw new LifecycleException("Error occurred when unsubscribe provider.");
        }

        client.close();
    }

    /**
     * 获得服务提供者实例的 Zookeeper 路径。使用 URLEncoder 编码 {@code providerUrl} 作为节点名称。
     * e.g. /z-rpc/x.xx.xxx/providers/x.xx.xxx?a=b
     */
    protected String makeProviderPath(String interfaceName, String providerUrl)
        throws UnsupportedEncodingException {
        String node = URLEncoder.encode(providerUrl, CHARSET);
        return ZKPaths.makePath(ROOT, interfaceName, PROVIDERS, node);
    }

    /**
     * 获得对应接口的 Zookeeper 路径。e.g. /z-rpc/x.xx.xxx/providers
     */
    protected String makeProviderPath(String interfaceName) {
        return ZKPaths.makePath(ROOT, interfaceName, PROVIDERS);
    }

    /**
     * 获得服务提供者实例的 Zookeeper 节点名称。e.g. x.xx.xxx?a=b
     */
    protected String getProviderUrl(ProviderConfig providerConfig, ProviderInterfaceConfig interfaceConfig) {
        Provider provider = new Provider();
        provider.setIp(providerConfig.getHost());
        provider.setPort(providerConfig.getPort());
        provider.setWarmup(providerConfig.getWarmup());
        provider.setWeight(providerConfig.getWeight());
        provider.setService(interfaceConfig.getInterfaceName());
        return provider.toUrl();
    }

    /**
     * 解析包含 {@code Provider} 信息的 {@code nodePath}。{@code nodePath} 是通过 URLEncode 处理的字符串。
     *
     * @param nodePath Zookeeper 节点路径
     * @return 解析 {@code nodePath} 获得的 {@code Provider}
     * @throws UnsupportedEncodingException 字符集异常
     */
    protected Provider parseProviderByPath(String nodePath) throws UnsupportedEncodingException {
        String zkProviderUrl = nodePath.substring(nodePath.lastIndexOf(ZKPaths.PATH_SEPARATOR) + 1);
        String providerUrl = URLDecoder.decode(zkProviderUrl, CHARSET);
        return Provider.parseProvider(providerUrl);
    }

    /**
     * 解析包含 {@code Provider} 信息的 {@code nodeName}。{@code nodeName} 是通过 URLEncode 处理的字符串。
     *
     * @param nodeName Zookeeper 节点名称
     * @return 解析 {@code nodeName} 获得的 {@code Provider}
     * @throws UnsupportedEncodingException 字符集异常
     */
    protected Provider parseProvider(String nodeName) throws UnsupportedEncodingException {
        String providerUrl = URLDecoder.decode(nodeName, CHARSET);
        return Provider.parseProvider(providerUrl);
    }
}
