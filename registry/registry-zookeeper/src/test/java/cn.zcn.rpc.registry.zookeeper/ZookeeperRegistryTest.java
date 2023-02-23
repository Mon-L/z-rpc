package cn.zcn.rpc.registry.zookeeper;

import cn.zcn.rpc.bootstrap.consumer.ConsumerInterfaceConfig;
import cn.zcn.rpc.bootstrap.provider.ProviderConfig;
import cn.zcn.rpc.bootstrap.provider.ProviderInterfaceConfig;
import cn.zcn.rpc.bootstrap.registry.Provider;
import cn.zcn.rpc.bootstrap.registry.ProviderListener;
import cn.zcn.rpc.bootstrap.registry.RegistryConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.data.Stat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * @author zicung
 */
public class ZookeeperRegistryTest {

    private TestingServer testingServer;
    private CuratorFramework client;
    private ZookeeperRegistry zkRegister;
    private ProviderConfig providerConfig;

    @Before
    public void before() throws Exception {
        testingServer = new TestingServer(8769);
        testingServer.start();

        providerConfig = new ProviderConfig();
        providerConfig.setHost("1.2.3.4");
        providerConfig.setPort(8888);

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setType(ZookeeperRegistry.ZOOKEEPER);
        registryConfig.setUrl(testingServer.getConnectString());

        zkRegister = new ZookeeperRegistry(registryConfig);
        zkRegister.start();

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(registryConfig.getUrl(), retryPolicy);
        client.start();
    }

    @After
    public void after() throws Exception {
        zkRegister.stop();
        client.close();
        testingServer.stop();
    }

    @Test
    public void testRegister() throws Exception {
        // register interfaces
        List<ProviderInterfaceConfig> registeredInterfaces = registerInterfaces(10);

        // verify providers
        for (int i = 0; i < 10; i++) {
            ProviderInterfaceConfig interfaceConfig = registeredInterfaces.get(i);

            Stat stat = client.checkExists()
                .forPath(
                    ZKPaths.makePath(ZookeeperRegistry.ROOT, interfaceConfig.getUniqueName(),
                        ZookeeperRegistry.PROVIDERS));

            assertThat(stat).isNotNull();
        }
    }

    @Test
    public void testUnregister() throws Exception {
        // register interfaces
        List<ProviderInterfaceConfig> registeredInterfaces = registerInterfaces(12);

        // unregister interfaces
        List<ProviderInterfaceConfig> unregisteredInterfaces = registeredInterfaces.subList(0, 5);
        zkRegister.unregister(providerConfig, unregisteredInterfaces);

        List<ProviderInterfaceConfig> remainingInterfaces = registeredInterfaces.subList(unregisteredInterfaces.size(),
            registeredInterfaces.size());

        for (ProviderInterfaceConfig interfaceConfig : unregisteredInterfaces) {
            String providerUrl = zkRegister.getProviderUrl(providerConfig, interfaceConfig);
            Stat stat = client.checkExists()
                .forPath(zkRegister.makeProviderPath(interfaceConfig.getUniqueName(), providerUrl));

            assertThat(stat).isNull();
        }

        for (ProviderInterfaceConfig interfaceConfig : remainingInterfaces) {
            String providerUrl = zkRegister.getProviderUrl(providerConfig, interfaceConfig);
            Stat stat = client.checkExists()
                .forPath(zkRegister.makeProviderPath(interfaceConfig.getUniqueName(), providerUrl));

            assertThat(stat).isNotNull();
        }
    }

    @Test
    public void testLoadProviders() {
        List<ProviderInterfaceConfig> registeredInterfaces = registerInterfaces(10);

        for (ProviderInterfaceConfig providerInterfaceConfig : registeredInterfaces) {
            ConsumerInterfaceConfig consumerInterfaceConfig = new ConsumerInterfaceConfig();
            consumerInterfaceConfig.setInterfaceName(providerInterfaceConfig.getInterfaceName());

            List<Provider> providers = zkRegister.loadProviders(consumerInterfaceConfig);
            assertThat(providers.size()).isEqualTo(1);
            assertThat(providers.get(0).getIp()).isEqualTo(providerConfig.getHost());
            assertThat(providers.get(0).getPort()).isEqualTo(providerConfig.getPort());
        }
    }

    @Test
    public void testLoadProviderButNoProviders() {
        ConsumerInterfaceConfig consumerInterfaceConfig = new ConsumerInterfaceConfig();
        consumerInterfaceConfig.setInterfaceName("cn.zcn.rpc.test.interface.xxx");

        List<Provider> providers = zkRegister.loadProviders(consumerInterfaceConfig);
        assertThat(providers.size()).isEqualTo(0);
    }

    @Test
    public void testSubscribeThenAddProvider() throws Exception {
        ConsumerInterfaceConfig consumerInterfaceConfig = new ConsumerInterfaceConfig();
        consumerInterfaceConfig.setInterfaceName("x.xx.xxx");

        List<Provider> providers = new ArrayList<>(); // 服务提供者列表
        CountDownLatch addProviderLatch = new CountDownLatch(10);

        //订阅服务提供者列表
        zkRegister.subscribe(consumerInterfaceConfig, new ProviderListener() {
            @Override
            public void removeProvider(Provider provider) {
            }

            @Override
            public void addProvider(Provider provider) {
                providers.add(provider);
                addProviderLatch.countDown();
            }

            @Override
            public void updateProviders(List<Provider> newProviders) {
            }
        });

        // 添加服务提供者
        ProviderInterfaceConfig providerInterfaceConfig = new ProviderInterfaceConfig();
        providerInterfaceConfig.setInterfaceName(consumerInterfaceConfig.getInterfaceName());
        List<ProviderConfig> providerConfigs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ProviderConfig p = new ProviderConfig();
            p.setHost("0.0.0." + i);
            p.setPort(9873);

            zkRegister.register(p, Collections.singletonList(providerInterfaceConfig));
            providerConfigs.add(p);
        }

        if (addProviderLatch.await(1, TimeUnit.SECONDS)) {
            //校验服务提供者列表与订阅获取的服务提供者列表是否一致
            assertThat(providers.size()).isEqualTo(providerConfigs.size());

            int remaining = providerConfigs.size();
            for (ProviderConfig config : providerConfigs) {
                for (Provider provider : providers) {
                    if (provider.getIp().equals(config.getHost()) &&
                        provider.getPort() == config.getPort()) {
                        remaining--;
                        break;
                    }
                }
            }

            if (remaining > 0) {
                fail("Provider size is inconsistency.");
            }
        } else {
            fail("Should not reach here.");
        }
    }

    @Test
    public void testSubscribeThenRemoveProvider() throws InterruptedException {
        ConsumerInterfaceConfig consumerInterfaceConfig = new ConsumerInterfaceConfig();
        consumerInterfaceConfig.setInterfaceName("x.xx.xxx");

        ProviderInterfaceConfig providerInterfaceConfig = new ProviderInterfaceConfig();
        providerInterfaceConfig.setInterfaceName(consumerInterfaceConfig.getInterfaceName());

        Set<Provider> providers = new HashSet<>(); // 服务提供者列表
        CountDownLatch removeProviderLatch = new CountDownLatch(10);

        //订阅服务提供者列表
        zkRegister.subscribe(consumerInterfaceConfig, new ProviderListener() {
            @Override
            public void removeProvider(Provider provider) {
                providers.remove(provider);
                removeProviderLatch.countDown();
            }

            @Override
            public void addProvider(Provider provider) {
                providers.add(provider);
            }

            @Override
            public void updateProviders(List<Provider> newProviders) {
            }
        });

        // 添加服务提供者
        List<ProviderConfig> providerConfigs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ProviderConfig p = new ProviderConfig();
            p.setHost("0.0.0." + i);
            p.setPort(9873);

            zkRegister.register(p, Collections.singletonList(providerInterfaceConfig));
            providerConfigs.add(p);
        }

        TimeUnit.MILLISECONDS.sleep(100);

        // 下线所有服务提供者
        for (ProviderConfig p : providerConfigs) {
            zkRegister.unregister(p, Collections.singletonList(providerInterfaceConfig));
        }

        if (removeProviderLatch.await(1, TimeUnit.SECONDS)) {
            //校验服务提供者列表与订阅获取的服务提供者列表是否一致
            assertThat(providers.size()).isEqualTo(0);
        } else {
            fail("Should not reach here.");
        }
    }

    @Test
    public void testUnsubscribe() throws InterruptedException {
        ConsumerInterfaceConfig consumerInterfaceConfig = new ConsumerInterfaceConfig();
        consumerInterfaceConfig.setInterfaceName("x.xx.xxx");

        ProviderInterfaceConfig providerInterfaceConfig = new ProviderInterfaceConfig();
        providerInterfaceConfig.setInterfaceName(consumerInterfaceConfig.getInterfaceName());

        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(5);

        //订阅服务提供者列表
        zkRegister.subscribe(consumerInterfaceConfig, new ProviderListener() {
            @Override
            public void removeProvider(Provider provider) {
            }

            @Override
            public void addProvider(Provider provider) {
                latch.countDown();
                count.getAndIncrement();
            }

            @Override
            public void updateProviders(List<Provider> newProviders) {
            }
        });

        int index = 0;

        // 注册服务提供者
        while (index++ < 5) {
            ProviderConfig p = new ProviderConfig();
            p.setHost("0.0.0." + index);
            p.setPort(9873);
            zkRegister.register(p, Collections.singletonList(providerInterfaceConfig));
        }
        if (latch.await(1, TimeUnit.SECONDS)) {
            assertThat(count.get()).isEqualTo(5);
        } else {
            fail("Should not reach here.");
        }

        // 取消订阅
        zkRegister.unsubscribe(consumerInterfaceConfig);

        // 继续注册服务提供者
        while (index++ < 10) {
            ProviderConfig p = new ProviderConfig();
            p.setHost("0.0.0." + index);
            p.setPort(9873);
            zkRegister.register(p, Collections.singletonList(providerInterfaceConfig));
        }
        TimeUnit.MILLISECONDS.sleep(300);

        // 取消订阅后，服务提供者的数量不会再增加
        assertThat(count.get()).isEqualTo(5);
    }

    private List<ProviderInterfaceConfig> registerInterfaces(int num) {
        List<ProviderInterfaceConfig> interfaceConfigs = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            ProviderInterfaceConfig interfaceConfig = new ProviderInterfaceConfig();
            interfaceConfig.setInterfaceName("cn.zcn.rpc.test.interface-" + i);
            interfaceConfigs.add(interfaceConfig);
        }

        zkRegister.register(providerConfig, interfaceConfigs);
        return interfaceConfigs;
    }
}
