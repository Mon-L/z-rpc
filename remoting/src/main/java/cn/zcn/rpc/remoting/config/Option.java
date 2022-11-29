package cn.zcn.rpc.remoting.config;

public class Option<T> {

    private final String name;
    private final T defaultValue;

    public Option(String name, T defaultValue) {
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
