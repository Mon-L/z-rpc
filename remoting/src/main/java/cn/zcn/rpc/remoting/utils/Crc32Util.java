package cn.zcn.rpc.remoting.utils;

import java.util.zip.CRC32;

/**
 * CRC32 工具
 *
 * @author zicung
 */
public class Crc32Util {

    /**
     * 计算 CRC32 校验码
     *
     * @param content 待计算内容
     * @return CRC32
     */
    public static int calculate(byte[] content) {
        CRC32 crc = new CRC32();
        crc.update(content);
        return (int) crc.getValue();
    }
}
