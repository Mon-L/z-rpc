package cn.zcn.rpc.example;

import cn.zcn.rpc.bootstrap.provider.ProviderBootstrap;
import cn.zcn.rpc.bootstrap.provider.ProviderConfig;
import cn.zcn.rpc.bootstrap.provider.ProviderInterfaceConfig;
import cn.zcn.rpc.bootstrap.registry.RegistryConfig;

public class Provider {

    public static void main(String[] args) throws InterruptedException {
        ProviderConfig providerConfig = new ProviderConfig();

        StudentService studentServiceImpl = new StudentServiceImpl();
        ProviderInterfaceConfig studentServiceConfig = new ProviderInterfaceConfig(StudentService.class, studentServiceImpl);
        studentServiceConfig.setVersion("1.0.1");
        providerConfig.addInterfaceConfig(studentServiceConfig);

        RegistryConfig nacosRegistryConfig = new RegistryConfig();
        nacosRegistryConfig.setType("nacos");
        nacosRegistryConfig.setUrl("127.0.0.1:8848");
        providerConfig.addRegistryConfig(nacosRegistryConfig);

        ProviderBootstrap bootstrap = new ProviderBootstrap(providerConfig);
        bootstrap.start();

        Runtime.getRuntime().addShutdownHook(new Thread(bootstrap::stop));

        while (true) {
            Thread.sleep(10000);
        }
    }
}
