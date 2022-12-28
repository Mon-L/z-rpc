package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.*;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;

public class ProtocolManagerTest {

    private ProtocolManager protocolManager;

    @Before
    public void before() {
        protocolManager = new ProtocolManager();
    }

    @Test
    public void testRegister() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() {
                protocolManager.registerProtocol(null, new NoopProtocol());
            }
        });

        Assertions.assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() {
                protocolManager.registerProtocol(ProtocolCode.from((byte) 1, (byte) 2), null);
            }
        });

        ProtocolCode protocolCode = ProtocolCode.from((byte) 1, (byte) 2);
        Protocol protocol = new NoopProtocol();

        protocolManager.registerProtocol(protocolCode, protocol);

        Assertions.assertThat(protocolManager.getProtocol(protocolCode)).isEqualTo(protocol);
    }

    @Test
    public void testUnregister() {
        ProtocolCode protocolCode = ProtocolCode.from((byte) 1, (byte) 2);
        Protocol protocol = new NoopProtocol();

        protocolManager.registerProtocol(protocolCode, protocol);
        Assertions.assertThat(protocolManager.getProtocol(protocolCode)).isEqualTo(protocol);

        protocolManager.unregisterProtocol(protocolCode);
        Assertions.assertThat(protocolManager.getProtocol(protocolCode)).isNull();
    }

    private static class NoopProtocol implements Protocol {

        @Override
        public ProtocolCode getProtocolCode() {
            return null;
        }

        @Override
        public ProtocolEncoder getEncoder() {
            return null;
        }

        @Override
        public ProtocolDecoder getDecoder() {
            return null;
        }

        @Override
        public CommandFactory getCommandFactory() {
            return null;
        }

        @Override
        public CommandHandler<ICommand> getCommandHandler(CommandCode cmd) {
            return null;
        }

        @Override
        public HeartbeatTrigger getHeartbeatTrigger() {
            return null;
        }

        @Override
        public void registerCommandHandler(CommandCode cmd, CommandHandler<ICommand> handler) {

        }
    }
}
