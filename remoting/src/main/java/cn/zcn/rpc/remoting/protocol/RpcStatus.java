package cn.zcn.rpc.remoting.protocol;

public enum RpcStatus {

    OK((short) 0x0000, "OK"),
    INTERNAL_SERVER_ERROR((short) 0x0001, "Internal Server Error"),
    UNSUPPORTED_PROTOCOL((short) 0x0002, "Unsupported Protocol"),
    UNSUPPORTED_COMMAND((short) 0x0003, "Unsupported Command"),
    UNSUPPORTED_SERIALIZER((short) 0x0004, "Unsupported Serializer"),
    SERIALIZATION_ERROR((short) 0x0005, "Serialization error"),
    DESERIALIZATION_ERROR((short) 0x0006, "deserialization error"),
    NO_REQUEST_PROCESSOR((short) 0x0007, "No request processor");

    private final short value;
    private final String reasonPhrase;

    RpcStatus(short value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    public short getValue() {
        return value;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    @Override
    public String toString() {
        return this.value + ":" + this.name();
    }

    public static RpcStatus valueOf(int statusCode) {
        RpcStatus status = resolve(statusCode);
        if (status == null) {
            throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
        } else {
            return status;
        }
    }

    public static RpcStatus resolve(int statusCode) {
        RpcStatus[] values = values();

        for (RpcStatus status : values) {
            if (status.value == statusCode) {
                return status;
            }
        }

        return null;
    }
}
