package cn.zcn.rpc.bootstrap;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Rpc 请求，包含对远程接口方法调用的接口名、方法名、方法参数类型、参数等数据。
 *
 * @author zicung
 */
public class RpcRequest implements Serializable {

    /** 接口名 */
    private String clazz;

    /** 方法名 */
    private String methodName;

    /** 方法参数 */
    private String[] parameterTypes = new String[0];

    /** 调用参数 */
    private Object[] parameters = new String[0];

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    /**
     * 获取远程接口方法唯一标识，格式为：
     * <pre>
     * {class}.{method}[{parameter types}]，e.g. "cn.zcn.rpc.Example.method[int, java.lang.String]"
     * </pre>
     *
     * @param request 当前请求
     * @return 方法唯一标识
     */
    public String getIdentifier(RpcRequest request) {
        return request.getClazz() + "." + request.getMethodName() + Arrays.toString(request.getParameterTypes());
    }

    public static RpcRequest from(Method method, Object[] parameters) {
        RpcRequest request = new RpcRequest();
        request.setClazz(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());

        request.setParameters(parameters);

        int i = 0;
        String[] parameterTypes = new String[method.getParameterTypes().length];
        for (Class<?> t : method.getParameterTypes()) {
            parameterTypes[i++] = t.getName();
        }
        request.setParameterTypes(parameterTypes);

        return request;
    }
}
