package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.CommandCode;
import cn.zcn.rpc.remoting.protocol.CommandFactory;
import cn.zcn.rpc.remoting.protocol.ICommand;
import cn.zcn.rpc.remoting.protocol.ProtocolCode;
import io.netty.util.AttributeKey;

/**
 * 协议抽象接口
 */
public interface Protocol {

    AttributeKey<ProtocolCode> PROTOCOL = AttributeKey.valueOf("protocol");

    ProtocolCode getProtocolCode();

    /**
     * 获取协议编码器
     *
     * @return {@link ProtocolEncoder}
     */
    ProtocolEncoder getEncoder();

    /**
     * 获取协议解码器
     *
     * @return {@link ProtocolDecoder}
     */
    ProtocolDecoder getDecoder();

    /**
     * 获取命令工厂，用于创建协议命令
     */
    CommandFactory getCommandFactory();

    /**
     * 获取命令处理器
     *
     * @param cmd 命令码
     * @return {@link CommandHandler}
     */
    CommandHandler<ICommand> getCommandHandler(CommandCode cmd);

    /**
     * 注册命令处理器
     *
     * @param cmd     命令码
     * @param handler {@link CommandHandler}
     */
    void registerCommandHandler(CommandCode cmd, CommandHandler<ICommand> handler);
}
