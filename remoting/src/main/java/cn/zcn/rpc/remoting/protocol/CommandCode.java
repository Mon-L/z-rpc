package cn.zcn.rpc.remoting.protocol;

/** @author zicung */
public enum CommandCode {
    /** 心跳命令 */
    HEARTBEAT((short) 1),

    /** 请求命令 */
    REQUEST((short) 2),

    /** 响应命令 */
    RESPONSE((short) 3);

    private final short value;

    CommandCode(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }

    public static CommandCode valueOf(short value) {
        switch (value) {
            case 1:
                return HEARTBEAT;
            case 2:
                return REQUEST;
            case 3:
                return RESPONSE;
            default:
                throw new IllegalArgumentException("Unknown command code : " + value);
        }
    }
}
