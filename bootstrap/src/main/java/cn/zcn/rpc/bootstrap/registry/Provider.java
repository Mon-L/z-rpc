package cn.zcn.rpc.bootstrap.registry;

import java.util.Objects;

/**
 * 服务提供者信息
 */
public class Provider {

    private String ip;
    private int port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Provider)) return false;

        Provider that = (Provider) o;

        if (port != that.port) return false;
        return Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }
}
