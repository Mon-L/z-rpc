package cn.zcn.rpc.bootstrap.provider;

import cn.zcn.rpc.bootstrap.InterfaceConfig;
import cn.zcn.rpc.bootstrap.RpcConfigs;

import java.util.List;

/**
 * 服务提供者接口配置
 *
 * @author zicung
 */
public class ProviderInterfaceConfig extends InterfaceConfig {

    /** 接口实现类 */
    private Object impl;

    /** 过滤器 */
    private List<String> filters = RpcConfigs.getList(RpcConfigs.FILTERS);

    public Object getImpl() {
        return impl;
    }

    public void setImpl(Object impl) {
        this.impl = impl;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    @Override
    public Class<?> getInterfaceClass() {
        Class<?> klass = super.getInterfaceClass();

        if (!klass.isAssignableFrom(impl.getClass())) {
            throw new IllegalArgumentException("Impl is not subtype of clazz.");
        }

        return klass;
    }
}
