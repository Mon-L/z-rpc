package cn.zcn.rpc.example;

import cn.zcn.rpc.bootstrap.consumer.ConsumerBootstrap;
import cn.zcn.rpc.bootstrap.consumer.ConsumerInterfaceConfig;
import cn.zcn.rpc.bootstrap.registry.RegistryConfig;
import org.apache.http.util.Asserts;

import java.util.Collections;

public class Consumer {

    public static void main(String[] args) throws Exception {
        ConsumerBootstrap bootstrap = new ConsumerBootstrap();
        bootstrap.start();

        //registry
        RegistryConfig nacosRegistryConfig = new RegistryConfig();
        nacosRegistryConfig.setType("nacos");
        nacosRegistryConfig.setUrl("127.0.0.1:8848");

        //interface config
        ConsumerInterfaceConfig studentServiceConfig = new ConsumerInterfaceConfig(StudentService.class);
        studentServiceConfig.setVersion("1.0.1");
        studentServiceConfig.setRegistryConfigs(Collections.singleton(nacosRegistryConfig));

        StudentService studentService = bootstrap.createProxy(studentServiceConfig);
        Student student = studentService.getStudentByName("foo");
        Asserts.check(student != null && student.getName().equals("foo"), "sdaf");

        Runtime.getRuntime().addShutdownHook(new Thread(bootstrap::stop));

        while (true) {
            Thread.sleep(10000);
        }
    }
}
