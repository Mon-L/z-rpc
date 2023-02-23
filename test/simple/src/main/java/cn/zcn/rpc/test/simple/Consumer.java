package cn.zcn.rpc.test.simple;

import cn.zcn.rpc.bootstrap.InvokeType;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.consumer.AsyncContext;
import cn.zcn.rpc.bootstrap.consumer.ConsumerBootstrap;
import cn.zcn.rpc.bootstrap.consumer.ConsumerInterfaceConfig;
import cn.zcn.rpc.bootstrap.registry.RegistryConfig;
import java.util.Collections;
import java.util.concurrent.Future;

import cn.zcn.rpc.test.student.Student;
import cn.zcn.rpc.test.student.StudentService;
import org.apache.http.util.Asserts;

public class Consumer {

    public static void main(String[] args) throws Throwable {
        syncInvoke();
        // asyncInvoke();
    }

    private static void asyncInvoke() throws Throwable {
        ConsumerBootstrap bootstrap = new ConsumerBootstrap();
        bootstrap.start();

        ConsumerInterfaceConfig interfaceConfig = new ConsumerInterfaceConfig();
        interfaceConfig.setInterfaceName(StudentService.class.getName());
        interfaceConfig.setInvokeType(InvokeType.FUTURE);

        withDirectUrl(interfaceConfig);
        // withRegistry(interfaceConfig);

        StudentService studentService = bootstrap.createProxy(interfaceConfig);
        Student student = studentService.getStudentByName("foo");
        Asserts.check(student == null, "error invocation");

        Future<RpcResponse> future = AsyncContext.getFuture();
        RpcResponse rpcResponse = future.get();
        student = (Student) rpcResponse.get();
        Asserts.check(student != null && student.getName().equals("foo"),
            "error invocation");

        bootstrap.stop();
    }

    private static void syncInvoke() {
        ConsumerBootstrap bootstrap = new ConsumerBootstrap();
        bootstrap.start();

        ConsumerInterfaceConfig interfaceConfig = new ConsumerInterfaceConfig();
        interfaceConfig.setInterfaceName(StudentService.class.getName());

        withDirectUrl(interfaceConfig);
        //withRegistry(interfaceConfig);

        StudentService studentService = bootstrap.createProxy(interfaceConfig);
        Student student = studentService.getStudentByName("foo");
        Asserts.check(student != null && student.getName().equals("foo"),
            "error invocation");

        bootstrap.stop();
    }

    private static void withDirectUrl(ConsumerInterfaceConfig interfaceConfig) {
        interfaceConfig.setProviderUrl("10.8.1.59:8008");
    }

    private static void withRegistry(ConsumerInterfaceConfig interfaceConfig) {
        RegistryConfig nacosRegistryConfig = new RegistryConfig();
        nacosRegistryConfig.setType("zookeeper");
        nacosRegistryConfig.setUrl("127.0.0.1:2181");
        interfaceConfig.setRegistryConfigs(Collections
            .singleton(nacosRegistryConfig));
    }
}
