package cn.zcn.rpc.bootstrap.provider;

import cn.zcn.rpc.bootstrap.InterfaceConfig;

/**
 * 服务提供者接口配置
 *
 * @author zicung
 */
public class ProviderInterfaceConfig extends InterfaceConfig {

    /**
     * 接口实现类
     */
    private final Object impl;

    public <T> ProviderInterfaceConfig(Class<T> clazz, T impl) {
        super(clazz);

        if (!clazz.isAssignableFrom(impl.getClass())) {
            throw new IllegalArgumentException("Impl is not subtype of clazz.");
        }

        this.impl = impl;
    }

    public Object getImpl() {
        return impl;
    }
}
