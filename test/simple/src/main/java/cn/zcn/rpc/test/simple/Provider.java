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
		providerConfig.port(8008);

		StudentService studentServiceImpl = new StudentServiceImpl();
		ProviderInterfaceConfig studentServiceConfig = new ProviderInterfaceConfig();
		studentServiceConfig.setInterfaceName(StudentService.class.getName());
		studentServiceConfig.setImpl(studentServiceImpl);
		providerConfig.addInterfaceConfig(studentServiceConfig);

		start(providerConfig);
		// startWithRegistry(providerConfig);
	}

	private static void start(ProviderConfig providerConfig) {
        ProviderBootstrap bootstrap = new ProviderBootstrap(providerConfig);
        bootstrap.start();

        Runtime.getRuntime().addShutdownHook(new Thread(bootstrap::stop));
    }
	private static void startWithRegistry(ProviderConfig providerConfig) {
        RegistryConfig nacosRegistryConfig = new RegistryConfig();
        nacosRegistryConfig.setType("nacos");
        nacosRegistryConfig.setUrl("127.0.0.1:8848");
        providerConfig.addRegistryConfig(nacosRegistryConfig);

        ProviderBootstrap bootstrap = new ProviderBootstrap(providerConfig);
        bootstrap.start();

        Runtime.getRuntime().addShutdownHook(new Thread(bootstrap::stop));
    }
}
