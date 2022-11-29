package cn.zcn.rpc.remoting.protocol;

public enum CommandType {
    REQUEST((byte) 1), REQUEST_ONEWAY((byte) 2), RESPONSE((byte) 3);

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
        }

        throw new IllegalArgumentException("Unknown command type : " + value);
    }
}
