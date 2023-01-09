package cn.zcn.rpc.remoting.protocol;

/**
 * 包含版本号的协议码
 *
 * @author zicung
 */
public class ProtocolCode {
    public static final int LENGTH = 2;

    /** 协议码 */
    private final byte code;

    /** 协议版本 */
    private final byte version;

    private ProtocolCode(byte code, byte version) {
        this.code = code;
        this.version = version;
    }

    public byte getCode() {
        return code;
    }

    public byte getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ProtocolCode)) {
            return false;
        }

        ProtocolCode that = (ProtocolCode) o;

        if (code != that.code) {
            return false;
        }

        return version == that.version;
    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + (int) version;
        return result;
    }

    @Override
    public String toString() {
        return "Protocol{code=" + code + ",version=" + version + "}";
    }

    public static ProtocolCode from(byte code, byte version) {
        return new ProtocolCode(code, version);
    }
}
