package cn.zcn.rpc.remoting.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OptionsImpl implements Options {

    private final ConcurrentMap<Option<?>, Object> options = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> T getOption(Option<T> option) {
        Object value = options.get(option);
        if (value == null) {
            return option.getDefaultValue();
        }

        return (T) value;
    }

    public <T> void setOption(Option<T> option, T value) {
        options.put(option, value);
    }
}
