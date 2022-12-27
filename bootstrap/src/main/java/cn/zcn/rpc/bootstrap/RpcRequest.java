package cn.zcn.rpc.bootstrap;

import java.io.Serializable;
import java.lang.reflect.Method;

public class RpcRequest implements Serializable {

    /**
     * 接口名
     */
    private String clazz;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 方法参数
     */
    private String[] parameterTypes;

    /**
     * 调用参数
     */
    private Object[] parameters;

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
