package cn.zcn.rpc.bootstrap.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author zicung
 */
public class Md5Util {
    private static final String CHARSET = "UTF-8";

    private static final MessageDigest MD5;

    static {
        try {
            MD5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] computeMd5(String key) {
        MessageDigest md5;
        try {
            md5 = (MessageDigest) MD5.clone();
            md5.update(key.getBytes(CHARSET));
        } catch (CloneNotSupportedException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return md5.digest();
    }
}
