package cn.zcn.rpc.bootstrap.provider;

import java.util.Objects;

public class InterfaceConfig {

    private final Class<?> interfaceClazz;
    private final Object instance;
    private String version;

    public InterfaceConfig(Class<?> interfaceClazz, Object instance) {
        if (!interfaceClazz.isInterface()) {
            throw new IllegalArgumentException("InterfaceClazz must be an interface.");
        }

        if (!interfaceClazz.isAssignableFrom(instance.getClass())) {
            throw new IllegalArgumentException("Instance is not subtype of interfaceClazz.");
        }

        this.interfaceClazz = interfaceClazz;
        this.instance = instance;
    }

    public Class<?> getInterfaceClazz() {
        return interfaceClazz;
    }

    public Object getInstance() {
        return instance;
    }

    public String getId() {
        return interfaceClazz.getName();
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InterfaceConfig)) return false;

        InterfaceConfig that = (InterfaceConfig) o;

        if (!Objects.equals(interfaceClazz, that.interfaceClazz))
            return false;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        int result = interfaceClazz != null ? interfaceClazz.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InterfaceConfig{" +
                "interfaceClazz=" + getId() +
                ", version='" + version + '\'' +
                '}';
    }
}
