package cn.zcn.rpc.remoting.protocol;

/**
 * Rpc 响应码
 *
 * @author zicung
 */
public enum RpcStatus {
    /** OK */
    OK((short) 0x0000, "OK"),

    /** 框架内部异常 */
    INTERNAL_SERVER_ERROR((short) 0x0001, "Internal Server Error"),

    /** 不支持的协议 */
    UNSUPPORTED_PROTOCOL((short) 0x0002, "Unsupported Protocol"),

    /** 不支持的命令 */
    UNSUPPORTED_COMMAND((short) 0x0003, "Unsupported BaseCommand"),

    /** 不支持的序列化器 */
    UNSUPPORTED_SERIALIZER((short) 0x0004, "Unsupported Serializer"),

    /** 序列化异常 */
    SERIALIZATION_ERROR((short) 0x0005, "Serialization error"),

    /** 反序列化异常 */
    DESERIALIZATION_ERROR((short) 0x0006, "deserialization error"),

    /** 找不到请求处理器 */
    NO_REQUEST_PROCESSOR((short) 0x0007, "No request processor"),

    /** 服务异常 */
    SERVICE_ERROR((short) 0x0008, "Service error"),

    /** 请求处理超时 */
    HANDLE_TIMEOUT((short) 0x0009, "Handle request timeout");

    /** 响应码 */
    private final short value;

    /** 响应信息 */
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
