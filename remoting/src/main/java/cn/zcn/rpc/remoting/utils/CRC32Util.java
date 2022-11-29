package cn.zcn.rpc.remoting.utils;

import java.util.zip.CRC32;

public class CRC32Util {

    public static int calculate(byte[] content) {
        CRC32 crc = new CRC32();
        crc.update(content);
        return (int) crc.getValue();
    }
}