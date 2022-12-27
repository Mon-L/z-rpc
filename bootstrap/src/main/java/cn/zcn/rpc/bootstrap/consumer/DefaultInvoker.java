package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
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

    DefaultInvoker(ConsumerInterfaceConfig interfaceConfig, RemotingClient remotingClient, ProvidersHolder providersHolder) {
        this.interfaceConfig = interfaceConfig;
        this.remotingClient = remotingClient;
        this.providersHolder = providersHolder;
    }

    @Override
    public RpcResponse invoke(RpcRequest request) throws RpcException {
        List<Provider> providers = providersHolder.getProviderInfos();

        if (providers.isEmpty()) {
            throw new RpcException("No service provider. Interface:{0}", interfaceConfig.getUniqueName());
        }

        Url url = buildUrl(providers.get(0));
        try {
            Future<RpcResponse> future = remotingClient.invoke(url, request, interfaceConfig.getTimeout() * 1000);
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
        return new Url.Builder(new InetSocketAddress(provider.getIp(), provider.getPort())).build();
    }
}
