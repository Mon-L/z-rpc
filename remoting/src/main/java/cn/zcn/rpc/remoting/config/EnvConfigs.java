package cn.zcn.rpc.remoting.config;

import static java.lang.System.getProperties;
import cn.zcn.rpc.remoting.utils.PropertiesUtils;

/**
 * 用于获取在系统变量中的用户配置。
 *
 * @author zicung
 */
public class EnvConfigs {

    /**
     * 获取系统变量字符串配置，如果配置不存在返回 null
     *
     * @param key key
     * @return value
     */
    public static String getString(String key) {
        return PropertiesUtils.getString(getProperties(), key);
    }

    /**
     * 获取系统变量字符串配置，如果配置不存在返回默认值
     *
     * @param key key
     * @return value
     */
    public static String getString(String key, String defaultValue) {
        return PropertiesUtils.getString(getProperties(), key, defaultValue);
    }

    /**
     * 获取系统变量布尔值配置，如果配置不存在返回 null
     *
     * @param key key
     * @return value
     */
    public static Boolean getBool(String key) {
        return PropertiesUtils.getBool(getProperties(), key);
    }

    /**
     * 获取系统变量布尔值配置，如果配置不存在返回默认值
     *
     * @param key key
     * @return value
     */
    public static boolean getBool(String key, boolean defaultValue) {
        return PropertiesUtils.getBool(getProperties(), key, defaultValue);
    }

    /**
     * 获取系统变量整型配置，如果配置不存在返回 null
     *
     * @param key key
     * @return value
     */
    public static Integer getInteger(String key) {
        return PropertiesUtils.getInteger(getProperties(), key);
    }

    /**
     * 获取系统变量整型配置，如果配置不存在返回默认值
     *
     * @param key key
     * @return value
     */
    public static int getInteger(String key, int defaultValue) {
        return PropertiesUtils.getInteger(getProperties(), key, defaultValue);
    }
}
