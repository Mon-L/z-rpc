package cn.zcn.rpc.remoting.protocol;

import java.io.Serializable;

public interface ICommand extends Serializable {

    ProtocolCode getProtocolCode();

    CommandType getCommandType();

    CommandCode getCommandCode();

    int getId();

    byte getSerializer();

    ProtocolSwitch getProtocolSwitch();

    byte[] getClazz();

    byte[] getContent();
}
