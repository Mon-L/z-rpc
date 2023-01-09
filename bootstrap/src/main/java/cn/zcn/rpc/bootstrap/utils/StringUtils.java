package cn.zcn.rpc.bootstrap.utils;

/** @author zicung */
public class StringUtils {

    /**
     * 判断字符串是否是空字符串
     *
     * @param str 字符串
     * @return {@code true}，{@code str} 为 {@code null} 或者 {@code str} 的长度为零；否则，返回 {@code false}。
     */
    public static boolean isEmptyOrNull(String str) {
        return str == null || str.length() == 0;
    }
}
