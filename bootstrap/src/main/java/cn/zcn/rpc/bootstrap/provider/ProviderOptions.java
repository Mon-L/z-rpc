package cn.zcn.rpc.bootstrap.provider;

import cn.zcn.rpc.bootstrap.RpcConfigs;
import cn.zcn.rpc.remoting.config.Option;
import cn.zcn.rpc.remoting.config.OptionsImpl;

/**
 * 服务提供者配置。优先级从高到低为：
 * <pre>
 * 1.RpcConfigs
 * 2.setOption(Option)
 * 3.Option defaultValue
 * </pre>
 */
public class ProviderOptions extends OptionsImpl {

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> T getOption(Option<T> option) {
        Object prop = RpcConfigs.getProperty(option.getName());
        return prop == null ? super.getOption(option) : (T) prop;
    }
}
