package cn.zcn.rpc.bootstrap;

import java.util.Objects;

/**
 * 包含版本号的接口配置。<p>
 * 重写了 {@code equals} 和 {@code hashCode} ，只要 {@code interfaceClass} 和 {@code version}
 * 相同则认为是相同的 {@code InterfaceConfig}。
 *
 * @author zicung
 */
public class InterfaceConfig {

    /**
     * 接口类
     */
    private final Class<?> interfaceClass;

    /**
     * 接口版本
     */
    private String version;

    public InterfaceConfig(Class<?> interfaceClass) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("InterfaceClass must be an interface.");
        }

        this.interfaceClass = interfaceClass;
    }

    /**
     * 获取接口唯一名称。<p>
     * 唯一名称的格式如下：
     * <pre>
     * 1. 版本号为Null: {interface}， cn.zcn.rpc.example.HelloService
     * 2. 版本号不为Null: {interface} + ":" + {version}， cn.zcn.rpc.example.HelloService:v1.0.0
     * </pre>
     */
    public String getUniqueName() {
        return getInterfaceClass().getName() + (getVersion() != null ? ":" + getVersion() : "");
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
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

        if (!Objects.equals(interfaceClass, that.interfaceClass)) {
            return false;
        }

        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        int result = interfaceClass != null ? interfaceClass.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InterfaceConfig{" +
                "interfaceClazz=" + interfaceClass.getName() +
                ", version='" + version + '\'' +
                '}';
    }
}
