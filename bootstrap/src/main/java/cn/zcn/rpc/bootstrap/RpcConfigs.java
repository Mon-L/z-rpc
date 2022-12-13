package cn.zcn.rpc.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * RPC 文件配置。优先级从高到低为:
 * <pre>
 *  1. rpc-config.properties
 *  2. META-INF/rpc-config.properties
 * </pre>
 */
public class RpcConfigs {

    private static final String[] CONFIG_PATH = new String[]{
            "META-INF/rpc-config.properties", "rpc-config.properties"};

    private static volatile Properties CFG;

    private static Properties getConfigs() {
        if (CFG == null) {
            synchronized (RpcConfigs.class) {
                if (CFG == null) {
                    CFG = new Properties();
                    loadConfigs();
                }
            }
        }

        return CFG;
    }

    /**
     * 加载 RPC 文件配置。根据 {@code CONFIG_PATH} 顺序读取，后面的文件配置会覆盖前面的文件配置
     */
    private static void loadConfigs() {
        ClassLoader classLoader = RpcConfigs.class.getClassLoader();

        for (String path : CONFIG_PATH) {
            try (InputStream input = classLoader.getResourceAsStream(path)) {
                if (input != null) {
                    Properties properties = new Properties();
                    properties.load(input);

                    Enumeration<?> names = properties.propertyNames();
                    while (names.hasMoreElements()) {
                        Object name = names.nextElement();
                        CFG.put(name, properties.get(name));
                    }
                }
            } catch (IOException e) {
                throw new RpcException("Failed to load config. Path:" + path);
            }
        }
    }

    public static Object getProperty(String key) {
        return getConfigs().get(key);
    }

    /**
     * 获取字符串配置，如果配置不存在返回 null
     *
     * @param key key
     * @return value
     */
    public static String getString(String key) {
        return getConfigs().getProperty(key);
    }

    /**
     * 获取布尔值配置，如果配置不存在返回 null
     *
     * @param key key
     * @return value
     */
    public static Boolean getBool(String key) {
        String val = getConfigs().getProperty(key);
        return val == null ? null : Boolean.valueOf(val);
    }

    /**
     * 获取整型配置，如果配置不存在返回 null
     *
     * @param key key
     * @return value
     */
    public static Integer getInteger(String key) {
        String val = getConfigs().getProperty(key);
        return val == null ? null : Integer.valueOf(val);
    }
}
