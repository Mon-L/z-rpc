package cn.zcn.rpc.bootstrap;

import java.io.Serializable;

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

    public String getMethodName() {
        return methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }
}
