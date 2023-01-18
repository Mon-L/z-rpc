package cn.zcn.rpc.test.simple;

import cn.zcn.rpc.bootstrap.consumer.ConsumerBootstrap;
import cn.zcn.rpc.bootstrap.consumer.ConsumerInterfaceConfig;
import cn.zcn.rpc.bootstrap.registry.RegistryConfig;
import java.util.Collections;

import cn.zcn.rpc.test.student.Student;
import cn.zcn.rpc.test.student.StudentService;
import org.apache.http.util.Asserts;

public class Consumer {

	public static void main(String[] args) {
		ConsumerBootstrap bootstrap = new ConsumerBootstrap();
		bootstrap.start();

		ConsumerInterfaceConfig interfaceConfig = new ConsumerInterfaceConfig();
		interfaceConfig.setInterfaceName(StudentService.class.getName());

		withDirectUrl(interfaceConfig);
		// withRegistry(interfaceConfig);

		StudentService studentService = bootstrap.createProxy(interfaceConfig);
		Student student = studentService.getStudentByName("foo");
		Asserts.check(student != null && student.getName().equals("foo"),
				"error invocation");

		bootstrap.stop();
	}

	private static void withDirectUrl(ConsumerInterfaceConfig interfaceConfig) {
		interfaceConfig.setProviderUrl("10.20.4.108:8008");
	}

	private static void withRegistry(ConsumerInterfaceConfig interfaceConfig) {
		RegistryConfig nacosRegistryConfig = new RegistryConfig();
		nacosRegistryConfig.setType("nacos");
		nacosRegistryConfig.setUrl("127.0.0.1:8848");
		interfaceConfig.setRegistryConfigs(Collections
				.singleton(nacosRegistryConfig));
	}
}
