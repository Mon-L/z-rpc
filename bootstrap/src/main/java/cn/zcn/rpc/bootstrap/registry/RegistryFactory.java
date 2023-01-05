package cn.zcn.rpc.bootstrap.registry;

import cn.zcn.rpc.bootstrap.extension.ExtensionLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册中心工厂。在同一个进程中，只会拥有一个相同 {@link RegistryConfig} 的 {@link Registry} 实例。
 *
 * @author zicung
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
