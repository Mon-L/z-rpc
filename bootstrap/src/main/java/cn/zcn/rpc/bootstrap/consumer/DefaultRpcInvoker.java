package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.InvokeType;
import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.consumer.loadbalance.LoadBalance;
import cn.zcn.rpc.bootstrap.extension.ExtensionLoader;
import cn.zcn.rpc.bootstrap.filter.*;
import cn.zcn.rpc.bootstrap.registry.Provider;
import cn.zcn.rpc.remoting.RemotingClient;
import cn.zcn.rpc.remoting.Url;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * {@code RpcInvoker} 默认实现，具有请求过滤、负载均衡功能。
 *
 * @author zicung
 */
public class DefaultRpcInvoker implements RpcInvoker {

    private volatile boolean started = false;
    private final RemotingClient remotingClient;
    private final ConsumerInterfaceConfig interfaceConfig;
    private final ProvidersHolder providersHolder;
    private FilterChain<ConsumerInvocation> filterChain;
    private LoadBalance loadBalance;

    public DefaultRpcInvoker(ConsumerInterfaceConfig interfaceConfig, RemotingClient remotingClient,
                             ProvidersHolder providersHolder) {
        this.interfaceConfig = interfaceConfig;
        this.remotingClient = remotingClient;
        this.providersHolder = providersHolder;
    }

    public void start() throws LifecycleException {
        filterChain = FilterChainBuilder.buildConsumerFilterChain(interfaceConfig.getFilters());
        filterChain.addLast(new RemotingInvocationFilter(remotingClient, interfaceConfig));

        loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class)
            .getExtension(interfaceConfig.getLoadBalance());

        started = true;
    }

    public void stop() throws LifecycleException {
        started = false;
    }

    @Override
    public RpcResponse invoke(RpcRequest request) throws RpcException {
        if (!started) {
            throw new RpcException("RpcInvoker is stopped.");
        }

        List<Provider> allProviders = providersHolder.getProviders();
        if (allProviders.isEmpty()) {
            throw new RpcException("No service provider. Interface:{}", interfaceConfig.getUniqueName());
        }

        Provider provider = loadBalance.select(allProviders, request);
        if (allProviders.isEmpty()) {
            throw new RpcException(
                "No service provider is selected after loadBalance. Interface:{}",
                interfaceConfig.getUniqueName());
        }

        ConsumerInvocation invocation = new ConsumerInvocation(request);
        invocation.setTimeout(interfaceConfig.getTimeout());
        invocation.setInvokeType(interfaceConfig.getInvokeType());
        invocation.setProvider(provider);

        try {
            filterChain.doFilter(invocation);
        } catch (Throwable t) {
            throw new RpcException(t.getMessage(), t);
        }

        if (invocation.getInvokeType() == InvokeType.FUTURE) {
            //异步调用，返回空响应
            RpcResponse asyncResponse = new RpcResponse();
            asyncResponse.set(null);
            AsyncContext.setFuture(invocation.getResponsePromise());
            return asyncResponse;
        } else {
            //同步调用，等待服务端响应
            RpcResponse syncResponse;
            try {
                syncResponse = invocation.getResponsePromise().get();
            } catch (Throwable t) {
                if (t instanceof ExecutionException) {
                    Throwable cause = t.getCause();
                    throw new RpcException(cause.getMessage(), cause);
                } else if (t instanceof RpcException) {
                    throw (RpcException) t;
                }
                throw new RpcException(t.getMessage(), t);
            }
            return syncResponse;
        }
    }

    private static class RemotingInvocationFilter implements ConsumerFilter {

        private final RemotingClient remotingClient;
        private final ConsumerInterfaceConfig interfaceConfig;

        private RemotingInvocationFilter(RemotingClient remotingClient, ConsumerInterfaceConfig interfaceConfig) {
            this.remotingClient = remotingClient;
            this.interfaceConfig = interfaceConfig;
        }

        @Override
        public void doFilter(ConsumerInvocation invocation, FilterContext<ConsumerInvocation> context)
            throws RpcException {
            Url url = convert2Url(invocation.getProvider());

            try {
                Future<RpcResponse> invokeFuture = remotingClient.invoke(url, invocation.getRequest(),
                    invocation.getTimeout());

                invokeFuture.addListener((GenericFutureListener<Future<RpcResponse>>) future -> {
                    // TODO cancel?
                    if (future.isSuccess()) {
                        invocation.getResponsePromise().complete(future.get());
                    } else {
                        invocation.getResponsePromise().completeExceptionally(future.cause());
                    }
                });

                context.doFilter(invocation);
            } catch (Throwable e) {
                throw new RpcException(e.getMessage(), e);
            }
        }

        private Url convert2Url(Provider provider) {
            return new Url.Builder(new InetSocketAddress(provider.getIp(), provider.getPort()))
                .maxConnectionNum(interfaceConfig.getMaxConnectionPerUrl())
                .build();
        }
    }
}
