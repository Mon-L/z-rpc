package cn.zcn.rpc.bootstrap.consumer.proxy;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.consumer.RpcInvoker;
import cn.zcn.rpc.bootstrap.extension.Extension;
import cn.zcn.rpc.remoting.exception.RemotingException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;

/** @author zicung */
@Extension("jdk")
public class JdkProxy implements Proxy {

    private static final String TO_STRING_METHOD_NAME = "toString";
    private static final String HASHCODE_METHOD_NAME = "hashcode";
    private static final String EQUALS_METHOD_NAME = "equals";

    @SuppressWarnings({ "unchecked" })
    @Override
    public <T> T createProxy(Class<?> clazz, RpcInvoker rpcInvoker) {
        InvocationHandler handler = new JdkInvocationHandler(rpcInvoker);
        Object instance = java.lang.reflect.Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(), new Class[] { clazz }, handler);
        return (T) instance;
    }

    private static class JdkInvocationHandler implements InvocationHandler {
        private final RpcInvoker rpcInvoker;

        private JdkInvocationHandler(RpcInvoker rpcInvoker) {
            this.rpcInvoker = rpcInvoker;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (TO_STRING_METHOD_NAME.equals(method.getName()) && args.length == 0) {
                return rpcInvoker.toString();
            } else if (HASHCODE_METHOD_NAME.equals(method.getName()) && args.length == 0) {
                return rpcInvoker.hashCode();
            } else if (EQUALS_METHOD_NAME.equals(method.getName()) && args.length == 1) {
                return rpcInvoker.equals(args[0]);
            }

            if (method.isDefault() && Modifier.isStatic(method.getModifiers())) {
                //jdk1.8, default methods in interface, static methods in interface
                return method.invoke(proxy, args);
            }

            RpcRequest request = RpcRequest.from(method, args);
            RpcResponse response = rpcInvoker.invoke(request);

            try {
                return response.get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                throw new RemotingException("Remoting method error. ErrorMsg: {}", cause.getMessage(), cause);
            }
        }
    }
}
