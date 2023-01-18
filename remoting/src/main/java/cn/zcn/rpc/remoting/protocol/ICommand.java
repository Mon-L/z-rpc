package cn.zcn.rpc.remoting.protocol;

import java.io.Serializable;

/** @author zicung */
public interface ICommand extends Serializable {
	/**
	 * 获取协议码
	 * 
	 * @return {@link ProtocolCode}
	 */
	ProtocolCode getProtocolCode();

	/**
	 * 获取命令类型
	 * 
	 * @return {@link CommandType}
	 */
	CommandType getCommandType();

	/**
	 * 获取命令码
	 * 
	 * @return {@link CommandCode}
	 */
	CommandCode getCommandCode();

	/**
	 * 获取 id
	 * 
	 * @return id
	 */
	int getId();

	/**
	 * 获取序列化器 id
	 * 
	 * @return 序列化器 id
	 */
	byte getSerializer();

	/**
	 * 获取协议选项
	 * 
	 * @return {@link ProtocolSwitch}
	 */
	ProtocolSwitch getProtocolSwitch();

	/**
	 * 获取协议内容的 Java 类型
	 * 
	 * @return byte[]
	 */
	byte[] getClazz();

	/**
	 * 获取协议内容
	 * 
	 * @return byte[]
	 */
	byte[] getContent();
}
