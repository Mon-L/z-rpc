package cn.zcn.rpc.test.simple;

import cn.zcn.rpc.bootstrap.provider.ProviderBootstrap;
import cn.zcn.rpc.bootstrap.provider.ProviderConfig;
import cn.zcn.rpc.bootstrap.provider.ProviderInterfaceConfig;
import cn.zcn.rpc.bootstrap.registry.RegistryConfig;
import cn.zcn.rpc.test.simple.service.StudentServiceImpl;
import cn.zcn.rpc.test.student.StudentService;

public class Provider {

    public static void main(String[] args) {
        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setPort(8008);

        StudentService studentServiceImpl = new StudentServiceImpl();
        ProviderInterfaceConfig studentServiceConfig = new ProviderInterfaceConfig();
        studentServiceConfig.setVersion("1.0.1");
        studentServiceConfig.setInterfaceName(StudentService.class.getName());
        studentServiceConfig.setImpl(studentServiceImpl);

        ProviderBootstrap bootstrap = new ProviderBootstrap(providerConfig);
        bootstrap.addInterface(studentServiceConfig);

        start(bootstrap);
        //startWithRegistry(bootstrap, providerConfig);
    }

    private static void start(ProviderBootstrap bootstrap) {
        bootstrap.start();
        Runtime.getRuntime().addShutdownHook(new Thread(bootstrap::stop));
    }

    private static void startWithRegistry(ProviderBootstrap bootstrap, ProviderConfig providerConfig) {
        RegistryConfig nacosRegistryConfig = new RegistryConfig();
        nacosRegistryConfig.setType("zookeeper");
        nacosRegistryConfig.setUrl("127.0.0.1:2181");
        providerConfig.addRegistryConfig(nacosRegistryConfig);

        bootstrap.start();
        Runtime.getRuntime().addShutdownHook(new Thread(bootstrap::stop));
    }
}
