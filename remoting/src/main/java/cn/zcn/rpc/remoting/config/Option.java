package cn.zcn.rpc.remoting.config;

/**
 * 这是一个包含名称和默认值的配置选项。
 *
 * <p>使用如下方式创建 {@code Option}：
 *
 * <pre>
 * {@code Option<Integer> age = valueOf("age", 1)}
 * </pre>
 *
 * @param <T> 选项值的类型
 * @author zicung
 */
public class Option<T> {
    private final String name;
    private final T defaultValue;

    private Option(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public static <T> Option<T> valueOf(String name, T value) {
        return new Option<>(name, value);
    }
}
