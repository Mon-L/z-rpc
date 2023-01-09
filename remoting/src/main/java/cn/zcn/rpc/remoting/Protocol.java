package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.*;
import io.netty.util.AttributeKey;

/**
 * 协议抽象接口
 *
 * @author zicung
 */
public interface Protocol {
    /**
     * 获取协议码
     *
     * @return 协议码
     */
    ProtocolCode getProtocolCode();

    /**
     * 获取协议编码器
     *
     * @return 编码器
     */
    ProtocolEncoder getEncoder();

    /**
     * 获取协议解码器
     *
     * @return 解码器
     */
    ProtocolDecoder getDecoder();

    /**
     * 获取命令工厂，用于创建协议命令
     *
     * @return 命令工厂
     */
    CommandFactory getCommandFactory();

    /**
     * 获取命令处理器
     *
     * @param cmd 命令码
     * @return 命令处理器
     */
    CommandHandler<ICommand> getCommandHandler(CommandCode cmd);

    /**
     * 获取心跳事件处理器
     *
     * @return 心跳事件处理器
     */
    HeartbeatTrigger getHeartbeatTrigger();

    /**
     * 注册命令处理器
     *
     * @param cmd 命令码
     * @param handler 命令处理器
     */
    void registerCommandHandler(CommandCode cmd, CommandHandler<ICommand> handler);
}
