package cn.zcn.rpc.remoting.protocol;

import java.util.BitSet;
import java.util.Objects;

/**
 * 协议选项，在协议中占一个字节的空间。
 *
 * @author zicung
 */
public class ProtocolSwitch {

    private final static int MAX_INDEX = 6;
    private final static int MIN_INDEX = 0;

    private final BitSet bits;

    private ProtocolSwitch(BitSet bits) {
        this.bits = bits;
    }

    public boolean isOn(int index) {
        checkIndex(index);
        return bits.get(index);
    }

    public void turnOn(int index) {
        checkIndex(index);
        bits.set(index, true);
    }

    public void turnOff(int index) {
        checkIndex(index);
        bits.set(index, false);
    }

    private void checkIndex(int index) {
        if (index < MIN_INDEX || index > MAX_INDEX) {
            throw new IllegalArgumentException("ProtocolSwitch index must between 0 and 6.");
        }
    }

    public byte toByte() {
        if (bits.size() == 0) {
            return 0;
        } else {
            byte[] bytes = bits.toByteArray();
            return bytes.length == 0 ? 0 : bytes[0];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ProtocolSwitch)) {
            return false;
        }

        ProtocolSwitch that = (ProtocolSwitch) o;

        return Objects.equals(bits, that.bits);
    }

    @Override
    public int hashCode() {
        return bits != null ? bits.hashCode() : 0;
    }

    public static ProtocolSwitch parse(byte b) {
        if (b < 0) {
            throw new IllegalArgumentException("ProtocolSwitch value must between 0 and 127.");
        }

        return new ProtocolSwitch(BitSet.valueOf(new byte[]{b}));
    }
}
