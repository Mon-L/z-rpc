package cn.zcn.rpc.bootstrap.registry;

import java.util.Objects;

/**
 * 服务提供者信息，包含服务提供者的ip、port等其他信息。
 *
 * @author zicung
 */
public class Provider {

    /** ip */
    private String ip;

    /** port */
    private int port;

    /** 服务权重 */
    private int weight = 5;

    /** 服务预热时间，毫秒 */
    private int warmup = 0;

    /** 服务启动时间 */
    private long startTime = 0;

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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWarmup() {
        return warmup;
    }

    public void setWarmup(int warmup) {
        this.warmup = warmup;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * 获取服务地址，格式为"ip:port"。
     *
     * @return 服务提供地址
     */
    public String getAddress() {
        return ip + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Provider)) {
            return false;
        }

        Provider that = (Provider) o;

        if (port != that.port) {
            return false;
        }

        return Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }
}
