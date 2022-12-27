package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.remoting.RemotingClient;
import cn.zcn.rpc.remoting.config.ClientOptions;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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
        remotingClient.option(ClientOptions.USE_CRC32, true);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T createProxy(ConsumerInterfaceConfig interfaceConfig) {
        InterfaceBootstrap interfaceBootstrap = new InterfaceBootstrap(interfaceConfig, remotingClient);
        interfaceBootstrap.start();

        interfaceBootstraps.put(interfaceConfig, interfaceBootstrap);
        Object proxy = interfaceBootstrap.createProxy();

        LOGGER.info("Interface proxy created. InterfaceBootstrap: {}", interfaceBootstrap);

        return (T) proxy;
    }

    @Override
    protected void doStop() throws LifecycleException {
        for (InterfaceBootstrap interfaceBootstrap : interfaceBootstraps.values()) {
            try {
                interfaceBootstrap.stop();
            } catch (Throwable t) {
                LOGGER.error("Failed to stop InterfaceBootstrap. InterfaceBootstrap: {}", interfaceBootstrap.toString(), t);
            }
        }

        remotingClient.stop();
    }
}
