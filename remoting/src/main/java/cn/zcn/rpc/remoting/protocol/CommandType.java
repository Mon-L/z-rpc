package cn.zcn.rpc.remoting.protocol;

/**
 * @author zicung
 */
public enum CommandType {
    /**
     * 双向请求，具有返回值
     */
    REQUEST((byte) 1),

    /**
     * 单向请求，没有返回值
     */
    REQUEST_ONEWAY((byte) 2),

    /**
     * 响应
     */
    RESPONSE((byte) 3);

    /**
     * 命令类型
     */
    private final short value;

    CommandType(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }

    public static CommandType valueOf(short value) {
        switch (value) {
            case 1:
                return REQUEST;
            case 2:
                return REQUEST_ONEWAY;
            case 3:
                return RESPONSE;
            default:
                throw new IllegalArgumentException("Unknown command type : " + value);
        }
    }
}
