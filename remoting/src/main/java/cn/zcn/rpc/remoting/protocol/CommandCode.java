package cn.zcn.rpc.remoting.protocol;

public enum CommandCode {
    HEARTBEAT((short) 1), REQUEST((short) 2), RESPONSE((short) 3);

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
        }

        throw new IllegalArgumentException("Unknown command code : " + value);
    }
}
