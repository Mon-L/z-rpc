package cn.zcn.rpc.bootstrap.registry;

import cn.zcn.rpc.bootstrap.extension.ExtensionLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册中心工厂。当服务提供者与服务消费者在同一个进程时，会共用 {@link Registry} 实例。
 */
public class RegistryFactory {

    private static volatile RegistryFactory instance;

    private final Map<RegistryConfig, Registry> registries = new HashMap<>();

    private RegistryFactory() {

    }

    public static RegistryFactory get() {
        if (instance == null) {
            synchronized (RegistryFactory.class) {
                if (instance == null) {
                    instance = new RegistryFactory();
                }
            }
        }
        return instance;
    }

    public Registry getOrCreateRegistry(RegistryConfig registryConfig) {
        Registry registry = registries.get(registryConfig);

        if (registry == null) {
            synchronized (this) {
                registry = registries.get(registryConfig);
                if (registry == null) {
                    ExtensionLoader<Registry> extensionLoader = ExtensionLoader.getExtensionLoader(Registry.class);
                    registry = extensionLoader.getExtension(registryConfig.getType(),
                            new Class<?>[]{RegistryConfig.class}, new Object[]{registryConfig});
                    registry.start();

                    registries.put(registryConfig, registry);
                }
            }
        }

        return registry;
    }
}
