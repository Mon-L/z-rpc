package cn.zcn.rpc.remoting.protocol;

import java.io.Serializable;

/** @author zicung */
public abstract class BaseCommand implements ICommand, Serializable {
    private ProtocolCode protocolCode;
    private CommandType commandType;
    private CommandCode commandCode;
    private int id;
    private byte serializer;
    private ProtocolSwitch protocolSwitch;
    private byte[] clazz;
    private byte[] content;

    public BaseCommand(ProtocolCode protocolCode, CommandType commandType) {
        this.protocolCode = protocolCode;
        this.commandType = commandType;
    }

    @Override
    public ProtocolCode getProtocolCode() {
        return protocolCode;
    }

    public void setProtocolCode(ProtocolCode protocolCode) {
        this.protocolCode = protocolCode;
    }

    @Override
    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    @Override
    public CommandCode getCommandCode() {
        return commandCode;
    }

    public void setCommandCode(CommandCode commandCode) {
        this.commandCode = commandCode;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public byte getSerializer() {
        return serializer;
    }

    public void setSerializer(byte serializer) {
        this.serializer = serializer;
    }

    @Override
    public ProtocolSwitch getProtocolSwitch() {
        return protocolSwitch;
    }

    public void setProtocolSwitch(ProtocolSwitch protocolSwitch) {
        this.protocolSwitch = protocolSwitch;
    }

    @Override
    public byte[] getClazz() {
        return clazz;
    }

    public void setClazz(byte[] clazz) {
        this.clazz = clazz;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
