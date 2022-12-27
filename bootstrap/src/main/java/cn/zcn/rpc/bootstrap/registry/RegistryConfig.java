package cn.zcn.rpc.bootstrap.registry;

import java.util.Objects;

public class RegistryConfig {

    private String type;

    private String url;

    private int connectTimeout;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistryConfig)) return false;

        RegistryConfig that = (RegistryConfig) o;

        if (!Objects.equals(type, that.type)) return false;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RegistryConfig{" +
                "type='" + type + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
