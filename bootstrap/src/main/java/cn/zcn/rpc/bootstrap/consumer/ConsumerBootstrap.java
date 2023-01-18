package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.remoting.RemotingClient;
import cn.zcn.rpc.remoting.config.ClientOptions;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC 客户端引导器。
 *
 * @author zicung
 */
public class ConsumerBootstrap extends AbstractLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerBootstrap.class);

    private RemotingClient remotingClient;
    private final Map<ConsumerInterfaceConfig, InterfaceBootstrap> interfaceBootstraps = new HashMap<>();

    @Override
    protected void doStart() throws LifecycleException {
        remotingClient = new RemotingClient();
        configRemotingClient(remotingClient);
        remotingClient.start();
    }

    protected void configRemotingClient(RemotingClient remotingClient) {
        remotingClient.option(ClientOptions.USE_CRC32, false);
    }

    @SuppressWarnings({ "unchecked" })
    public <T> T createProxy(ConsumerInterfaceConfig interfaceConfig) {
        InterfaceBootstrap interfaceBootstrap = new InterfaceBootstrap(interfaceConfig, remotingClient);
        interfaceBootstrap.start();

        interfaceBootstraps.put(interfaceConfig, interfaceBootstrap);
        Object proxy = interfaceBootstrap.createProxy();

        LOGGER.info("Interface proxy created，{}", interfaceConfig.getUniqueName());

        return (T) proxy;
    }

    public void destroyProxy(ConsumerInterfaceConfig interfaceConfig) {
        InterfaceBootstrap interfaceBootstrap = interfaceBootstraps.remove(interfaceConfig);
        if (interfaceBootstrap != null) {
            interfaceBootstrap.stop();
        }
    }

    @Override
    protected void doStop() throws LifecycleException {
        for (InterfaceBootstrap interfaceBootstrap : interfaceBootstraps.values()) {
            try {
                interfaceBootstrap.stop();
            } catch (Throwable t) {
                LOGGER.error(
                    "Failed to stop InterfaceBootstrap, {}", interfaceBootstrap.toString(), t);
            }
        }

        remotingClient.stop();
    }
}
