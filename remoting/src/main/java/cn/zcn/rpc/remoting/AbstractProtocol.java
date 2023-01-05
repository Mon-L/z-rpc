package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.CommandCode;
import cn.zcn.rpc.remoting.protocol.ICommand;
import cn.zcn.rpc.remoting.protocol.ProtocolCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract protocol
 *
 * @author zicung
 */
public abstract class AbstractProtocol implements Protocol {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProtocol.class);

    private final ProtocolCode protocolCode;
    private final Map<CommandCode, CommandHandler> handlers = new ConcurrentHashMap<>();

    public AbstractProtocol(ProtocolCode protocolCode) {
        this.protocolCode = protocolCode;
    }

    @Override
    public ProtocolCode getProtocolCode() {
        return protocolCode;
    }

    @Override
    public CommandHandler getCommandHandler(CommandCode cmd) {
        return handlers.get(cmd);
    }

    @Override
    public void registerCommandHandler(CommandCode cmd, CommandHandler handler) {
        if (cmd == null || handler == null) {
            throw new IllegalArgumentException("Both CommandCode and CommandHandler should not be null.");
        }

        CommandHandler<ICommand> oldHandler = handlers.put(cmd, handler);
        if (oldHandler != null) {
            LOGGER.warn("Protocol{} replace commandHandler by CommandCode{}", getProtocolCode(), cmd.name());
        }
    }
}
