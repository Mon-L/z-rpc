package cn.zcn.rpc.bootstrap.utils;

import java.lang.reflect.Method;

/**
 * 获取方法签名。<p>
 * 方法签名格式如下：
 * <pre>
 * 方法：void addUser(String name, int age, boolean locked, Callable<?> callable);
 * 方法签名：addUser:java.lang.String,int,boolean,java.util.concurrent.Callable
 * </pre>
 *
 * @author zicung
 */
public class MethodSignatureUtil {

    public static String getMethodSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName());

        if (method.getParameterTypes().length > 0) {
            sb.append(":");
            for (Class<?> param : method.getParameterTypes()) {
                sb.append(param.getName()).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    public static String getMethodSignature(String methodName, String[] parameterTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append(methodName);

        if (parameterTypes != null && parameterTypes.length > 0) {
            sb.append(":");
            for (String param : parameterTypes) {
                sb.append(param).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }
}
