package cn.zcn.rpc.bootstrap;

import cn.zcn.rpc.bootstrap.utils.StringUtils;

import java.util.Objects;

/**
 * 包含版本号的接口配置。
 *
 * <p>重写了 {@code equals} 和 {@code hashCode} ， {@code interfaceClass} 和 {@code version} 相同则认为是相同的
 * {@code InterfaceConfig}。
 *
 * @author zicung
 */
public class InterfaceConfig {

    /** 接口类 */
    private String interfaceName;

    /** 接口版本 */
    private String version;

    /**
     * 获取接口唯一名称。
     *
     * <p>唯一名称的格式如下：
     *
     * <pre>
     * 1. 版本号为Null: {interface}， cn.zcn.rpc.example.HelloService
     * 2. 版本号不为Null: {interface} + ":" + {version}， cn.zcn.rpc.example.HelloService:v1.0.0
     * </pre>
     */
    public String getUniqueName() {
        return interfaceName + (getVersion() != null ? ":" + getVersion() : "");
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Class<?> getInterfaceClass() {
        if (StringUtils.isEmptyOrNull(interfaceName)) {
            throw new IllegalArgumentException("InterfaceName must not be null.");
        }

        Class<?> klass;
        try {
            klass = Class.forName(interfaceName);
        } catch (ClassNotFoundException e) {
            throw new RpcException(e.getMessage(), e);
        }

        if (!klass.isInterface()) {
            throw new IllegalArgumentException("InterfaceClass must be an interface.");
        }

        return klass;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof InterfaceConfig)) {
            return false;
        }

        InterfaceConfig that = (InterfaceConfig) o;

        if (!Objects.equals(interfaceName, that.interfaceName)) {
            return false;
        }

        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        int result = interfaceName != null ? interfaceName.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InterfaceConfig{" + "interface=" + interfaceName + ", version='" + version + '\'' + '}';
    }
}
