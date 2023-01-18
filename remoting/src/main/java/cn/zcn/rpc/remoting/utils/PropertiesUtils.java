package cn.zcn.rpc.remoting.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author zicung
 */
public class PropertiesUtils {

    private static final char BLANK = ' ';

    /**
     * 获取字符串配置，如果配置不存在返回 null
     *
     * @param properties  properties
     * @param key key
     * @return value
     */
    public static String getString(Properties properties, String key) {
        return properties.getProperty(key);
    }

    /**
     * 获取字符串配置，如果配置不存在返回默认值
     *
     * @param properties  properties
     * @param key key
     * @return value
     */
    public static String getString(Properties properties, String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 获取布尔值配置，如果配置不存在返回 null
     *
     * @param properties  properties
     * @param key key
     * @return value
     */
    public static Boolean getBool(Properties properties, String key) {
        Object val = properties.get(key);
        return val == null ? null : Boolean.valueOf(val.toString());
    }

    /**
     * 获取布尔值配置，如果配置不存在返回默认值
     *
     * @param properties  properties
     * @param key key
     * @return value
     */
    public static boolean getBool(Properties properties, String key, boolean defaultValue) {
        Object val = properties.get(key);
        return val == null ? defaultValue : Boolean.parseBoolean(val.toString());
    }

    /**
     * 获取整型配置，如果配置不存在返回 null
     *
     * @param properties  properties
     * @param key key
     * @return value
     */
    public static Integer getInteger(Properties properties, String key) {
        Object val = properties.get(key);
        return val == null ? null : Integer.valueOf(val.toString());
    }

    /**
     * 获取整型配置，如果配置不存在返回默认值
     *
     * @param properties  properties
     * @param key key
     * @return value
     */
    public static int getInteger(Properties properties, String key, int defaultValue) {
        Object val = properties.get(key);
        return val == null ? defaultValue : Integer.parseInt(val.toString());
    }

    /**
     * 获取列表，多个值之间使用空格符分割。
     *
     * <pre>
     * a b c  returns  [a, b, c]
     * </pre>
     *
     * @param properties  properties
     * @param key key
     * @return list
     */
    public static List<String> getList(Properties properties, String key) {
        String val = properties.getProperty(key);
        if (val == null || val.trim().length() == 0) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        int l = 0, r = 0, len = val.length();
        while (r < len) {
            if (val.charAt(r) == BLANK) {
                if (l != r) {
                    list.add(val.substring(l, r));
                }

                // remove space
                while (++r < len && val.charAt(r) == BLANK) {
                }

                l = r;
            } else {
                r++;
            }
        }

        if (l != r) {
            list.add(val.substring(l, r));
        }

        return Collections.unmodifiableList(list);
    }
}
