package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.consumer.filter.Filter;
import cn.zcn.rpc.bootstrap.consumer.filter.FilterChainBuilder;
import cn.zcn.rpc.bootstrap.consumer.filter.FilterChainNode;
import cn.zcn.rpc.bootstrap.consumer.loadbalance.LoadBalance;
import cn.zcn.rpc.bootstrap.extension.ExtensionLoader;
import cn.zcn.rpc.bootstrap.registry.Provider;
import cn.zcn.rpc.remoting.RemotingClient;
import cn.zcn.rpc.remoting.Url;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * {@code RpcInvoker} 默认实现，具有请求过滤、负载均衡功能。
 *
 * @author zicung
 */
public class DefaultRpcInvoker implements RpcInvoker {

    private final RemotingClient remotingClient;
    private final ConsumerInterfaceConfig interfaceConfig;
    private final ProvidersHolder providersHolder;
    private FilterChainNode filterHead;
    private LoadBalance loadBalance;

    public DefaultRpcInvoker(ConsumerInterfaceConfig interfaceConfig, RemotingClient remotingClient,
                             ProvidersHolder providersHolder) {
        this.interfaceConfig = interfaceConfig;
        this.remotingClient = remotingClient;
        this.providersHolder = providersHolder;
    }

    public void init() {
        RemotingInvocationFilter remotingInvocationFilter = new RemotingInvocationFilter(remotingClient,
            interfaceConfig);
        filterHead = FilterChainBuilder.build(remotingInvocationFilter, interfaceConfig.getFilters());

        loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class)
            .getExtension(interfaceConfig.getLoadBalance());
    }

    @Override
    public RpcResponse invoke(RpcRequest request) throws RpcException {
        List<Provider> allProviders = providersHolder.getProviders();
        if (allProviders.isEmpty()) {
            throw new RpcException("No service provider. Interface:{0}", interfaceConfig.getUniqueName());
        }

        Provider provider = loadBalance.select(allProviders, request);
        if (allProviders.isEmpty()) {
            throw new RpcException(
                "No service provider is selected after loadBalance. Interface:{0}",
                interfaceConfig.getUniqueName());
        }

        return filterHead.invoke(provider, request);
    }

    private static class RemotingInvocationFilter implements Filter {

        private final RemotingClient remotingClient;
        private final ConsumerInterfaceConfig interfaceConfig;

        private RemotingInvocationFilter(RemotingClient remotingClient, ConsumerInterfaceConfig interfaceConfig) {
            this.remotingClient = remotingClient;
            this.interfaceConfig = interfaceConfig;
        }

        @Override
        public RpcResponse doFilter(Provider provider, RpcRequest request, FilterChainNode nextFilter)
            throws RpcException {
            Url url = buildUrl(provider);

            try {
                Future<RpcResponse> future = remotingClient.invoke(url, request, interfaceConfig.getTimeout());
                return future.get();
            } catch (Throwable e) {
                if (e instanceof ExecutionException) {
                    Throwable cause = e.getCause();
                    throw new RpcException(cause, cause.getMessage());
                }

                throw new RpcException(e, e.getMessage());
            }
        }

        private Url buildUrl(Provider provider) {
            return new Url.Builder(new InetSocketAddress(provider.getIp(), provider.getPort()))
                .maxConnectionNum(interfaceConfig.getMaxConnectionPerUrl())
                .build();
        }
    }
}
