package cn.zcn.rpc.jmeter;

import cn.zcn.rpc.bootstrap.consumer.ConsumerBootstrap;
import cn.zcn.rpc.bootstrap.consumer.ConsumerInterfaceConfig;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * @author zicung
 */
public class ZRpcSampler extends AbstractJavaSamplerClient implements Interruptible {
    private static final Logger LOGGER = LoggingManager.getLoggerForClass();

    private ConsumerBootstrap bootstrap;
    private ConsumerInterfaceConfig interfaceConfig;
    private Object instance;
    private Method method;
    private Object[] parameters;

    @Override
    public void setupTest(JavaSamplerContext context) {
        try {
            this.bootstrap = new ConsumerBootstrap();
            bootstrap.start();

            // interface config
            String providerUrl = context.getParameter("providerUrl");
            String className = context.getParameter("class");
            String methodName = context.getParameter("method");

            String parametersString = context.getParameter("parametersArgs");
            if (notNullOrBlank(parametersString)) {
                String[] parameterArgs = parametersString.trim().split(",");
                this.parameters = new Object[parameterArgs.length];

                System.arraycopy(parameterArgs, 0, parameters, 0, parameterArgs.length);
            }

            Class<?>[] parameterClass = null;
            String parameterTypeString = context.getParameter("parametersTypes");
            if (notNullOrBlank(parameterTypeString)) {
                String[] parameterTypes = parameterTypeString.trim().split(",");
                parameterClass = new Class[parameterTypes.length];

                for (int i = 0; i < parameterTypes.length; i++) {
                    parameterClass[i] = Class.forName(parameterTypes[i]);
                }
            }

            Class<?> klass = Class.forName(className);
            if (parameterClass != null && parameterClass.length > 0) {
                this.method = klass.getMethod(methodName, parameterClass);
            } else {
                this.method = klass.getMethod(methodName);
            }

            interfaceConfig = new ConsumerInterfaceConfig();
            interfaceConfig.setProviderUrl(providerUrl);
            interfaceConfig.setInterfaceName(className);
            this.instance = bootstrap.createProxy(interfaceConfig);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        params.addArgument("providerUrl", "10.8.1.59:8008");
        params.addArgument("class", "cn.zcn.rpc.test.student.StudentService");
        params.addArgument("method", "getStudentByName");
        params.addArgument("parametersTypes", "java.lang.String");
        params.addArgument("parametersArgs", "abc");
        return params;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.setSamplerData(getRequestData(context));
        result.setDataType(SampleResult.TEXT);

        result.sampleStart();
        try {
            if (parameters != null && parameters.length > 0) {
                method.invoke(instance, parameters);
            } else {
                method.invoke(instance);
            }
            result.sampleEnd();
            result.setSuccessful(true);
            result.setResponseMessageOK();
            result.setResponseCodeOK();
            result.setResponseData(result.getClass().getSimpleName(), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            result.sampleEnd();
            result.setSuccessful(false);
            result.setResponseData(e.toString(), StandardCharsets.UTF_8.name());
            LOGGER.error("Failed to invoke remoting interface method, " + e.getMessage(), e);
        }

        return result;
    }

    private String getRequestData(JavaSamplerContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Interface: ").append(context.getParameter("class")).append("\n");
        sb.append("Method: ").append(context.getParameter("method")).append("\n");
        sb.append("Proxy: ").append(interfaceConfig.getProxy()).append("\n");
        sb.append("Timeout: ").append(interfaceConfig.getTimeout()).append("\n");
        sb.append("LoadBalance: ").append(interfaceConfig.getLoadBalance()).append("\n");
        sb.append("ConnectionNums: ").append(interfaceConfig.getMaxConnectionPerUrl()).append("\n");
        return sb.toString();
    }

    public boolean notNullOrBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        this.bootstrap.stop();
    }

    @Override
    public boolean interrupt() {
        Thread.currentThread().interrupt();
        return true;
    }
}
