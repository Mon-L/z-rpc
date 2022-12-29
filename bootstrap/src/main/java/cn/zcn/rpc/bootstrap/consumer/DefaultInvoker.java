package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.consumer.filter.Filter;
import cn.zcn.rpc.bootstrap.consumer.filter.FilterChainBuilder;
import cn.zcn.rpc.bootstrap.consumer.filter.FilterChainNode;
import cn.zcn.rpc.bootstrap.registry.Provider;
import cn.zcn.rpc.remoting.RemotingClient;
import cn.zcn.rpc.remoting.Url;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DefaultInvoker implements Invoker {

    private final RemotingClient remotingClient;
    private final ConsumerInterfaceConfig interfaceConfig;
    private final ProvidersHolder providersHolder;
    private FilterChainNode filterHead;

    public DefaultInvoker(ConsumerInterfaceConfig interfaceConfig, RemotingClient remotingClient, ProvidersHolder providersHolder) {
        this.interfaceConfig = interfaceConfig;
        this.remotingClient = remotingClient;
        this.providersHolder = providersHolder;
    }

    public void init() {
        RemotingInvocationFilter remotingInvocationFilter = new RemotingInvocationFilter(remotingClient);
        remotingInvocationFilter.setTimeout(interfaceConfig.getTimeout());

        filterHead = FilterChainBuilder.build(remotingInvocationFilter, interfaceConfig.getFilters());
    }

    @Override
    public RpcResponse invoke(RpcRequest request) throws RpcException {
        List<Provider> allProviders = providersHolder.getProviders();

        if (allProviders.isEmpty()) {
            throw new RpcException("No service provider. Interface:{0}", interfaceConfig.getUniqueName());
        }

        //TODO loadbalancer
        Provider provider = allProviders.get(0);

        return filterHead.invoke(provider, request);
    }

    private static class RemotingInvocationFilter implements Filter {

        private final RemotingClient remotingClient;
        private int timeoutMillis = -1;

        private RemotingInvocationFilter(RemotingClient remotingClient) {
            this.remotingClient = remotingClient;
        }

        @Override
        public RpcResponse doFilter(Provider provider, RpcRequest request, FilterChainNode nextFilter) throws RpcException {
            Url url = buildUrl(provider);

            try {
                Future<RpcResponse> future = remotingClient.invoke(url, request, timeoutMillis);
                return future.get();
            } catch (Throwable e) {
                if (e instanceof ExecutionException) {
                    Throwable cause = e.getCause();
                    throw new RpcException(cause, cause.getMessage());
                }

                throw new RpcException(e, e.getMessage());
            }
        }

        private void setTimeout(int timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
        }

        private Url buildUrl(Provider provider) {
            return new Url.Builder(new InetSocketAddress(provider.getIp(), provider.getPort())).build();
        }
    }
}
