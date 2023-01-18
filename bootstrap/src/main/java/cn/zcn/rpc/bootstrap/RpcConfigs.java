package cn.zcn.rpc.bootstrap;

import cn.zcn.rpc.remoting.utils.PropertiesUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * RPC 文件配置，配置文件名为 rpc-config.properties。可以有多份该配置文件，配置文件对优先级从高到低为:
 *
 * <pre>
 * 1. rpc-config.properties
 * 2. META-INF/rpc-config.properties
 * </pre>
 *
 * @author zicung
 */
public class RpcConfigs {

    /** 服务提供者权重 */
    public static final String WEIGHT = "weight";

    /** 服务预热时间 */
    public static final String WARMUP = "warmup";

    /** 注册中心地址 */
    public static final String REGISTRY = "registry";

    /** 代理方式 */
    public static final String PROXY = "proxy";

    /** 路由算法 */
    public static final String ROUTER = "router";

    /** 与每个服务提供者的最大连接数, 默认值为 1 */
    public static final String MAX_CONNECTION_PER_URL = "maxConnectionPerUrl";

    /** 负载均衡算法 */
    public static final String LOAD_BALANCE = "loadBalance";

    /** 请求超时时间，单位秒 */
    public static final String TIMEOUT = "timeout";

    /** 忽略超时请求 */
    public static final String IGNORE_TIMEOUT_REQUEST = "ignoreTimeoutRequest";

    /** 过滤器列表 */
    public static final String FILTERS = "filters";

    private static final String[] CONFIG_PATH = new String[] { "META-INF/rpc-config.properties",
                                                               "rpc-config.properties" };

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

    /** 加载 RPC 文件配置。根据 {@code CONFIG_PATH} 顺序读取，后面的文件配置会覆盖前面的文件配置 */
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

    /**
     * 获取字符串配置，如果配置不存在返回 null
     *
     * @param key key
     * @return value
     */
    public static String getString(String key) {
        return PropertiesUtils.getString(getConfigs(), key);
    }

    /**
     * 获取字符串配置，如果配置不存在返回默认值
     *
     * @param key key
     * @return value
     */
    public static String getString(String key, String defaultValue) {
        return PropertiesUtils.getString(getConfigs(), key, defaultValue);
    }

    /**
     * 获取布尔值配置，如果配置不存在返回 null
     *
     * @param key key
     * @return value
     */
    public static Boolean getBool(String key) {
        return PropertiesUtils.getBool(getConfigs(), key);
    }

    /**
     * 获取布尔值配置，如果配置不存在返回默认值
     *
     * @param key key
     * @return value
     */
    public static Boolean getBool(String key, boolean defaultValue) {
        return PropertiesUtils.getBool(getConfigs(), key, defaultValue);
    }

    /**
     * 获取整型配置，如果配置不存在返回 null
     *
     * @param key key
     * @return value
     */
    public static Integer getInteger(String key) {
        return PropertiesUtils.getInteger(getConfigs(), key);
    }

    /**
     * 获取整型配置，如果配置不存在返回默认值
     *
     * @param key key
     * @return value
     */
    public static Integer getInteger(String key, int defaultValue) {
        return PropertiesUtils.getInteger(getConfigs(), key, defaultValue);
    }

    /**
     * 获取列表，多个值之间使用空格符分割。
     *
     * <pre>
     * a b c  returns  [a, b, c]
     * </pre>
     *
     * @param key key
     * @return list
     */
    public static List<String> getList(String key) {
        return PropertiesUtils.getList(getConfigs(), key);
    }
}
