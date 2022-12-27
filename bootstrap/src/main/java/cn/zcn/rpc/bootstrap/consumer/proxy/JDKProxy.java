package cn.zcn.rpc.bootstrap.consumer.proxy;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.consumer.Invoker;
import cn.zcn.rpc.bootstrap.extension.Extension;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

@Extension("jdk")
public class JDKProxy implements Proxy {

    @SuppressWarnings({"unchecked"})
    @Override
    public <T> T createProxy(Class<?> clazz, Invoker invoker) {
        InvocationHandler handler = new JDKInvocationHandler(invoker);
        Object instance = java.lang.reflect.Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{clazz}, handler);
        return (T) instance;
    }

    private static class JDKInvocationHandler implements InvocationHandler {
        private final Invoker invoker;

        private JDKInvocationHandler(Invoker invoker) {
            this.invoker = invoker;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("toString") && args.length == 0) {
                return invoker.toString();
            } else if (method.getName().equals("hashcode") && args.length == 0) {
                return invoker.hashCode();
            } else if (method.getName().equals("equals") && args.length == 1) {
                return invoker.equals(args[0]);
            }

            RpcRequest request = RpcRequest.from(method, args);
            RpcResponse response = invoker.invoke(request);

            try {
                return response.get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        }
    }
}
