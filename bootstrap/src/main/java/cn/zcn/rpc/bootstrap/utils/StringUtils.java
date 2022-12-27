package cn.zcn.rpc.bootstrap.utils;

public class StringUtils {

    public static boolean isEmptyOrNull(String str) {
        return str == null || str.length() == 0;
    }
}
