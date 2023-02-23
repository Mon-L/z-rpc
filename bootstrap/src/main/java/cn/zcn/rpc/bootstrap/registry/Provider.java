package cn.zcn.rpc.bootstrap.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 服务提供者信息，包含服务提供者的ip、port等其他信息。
 *
 * @author zicung
 */
public class Provider {

    private static final String WEIGHT = "weight";
    private static final String WARMUP = "warmup";
    private static final String START_TIME = "start-time";

    /** ip */
    private String ip;

    /** port */
    private int port;

    /** service */
    private String service;

    /** 服务权重 */
    private int weight = 5;

    /** 服务预热时间，毫秒 */
    private int warmup = 0;

    /** 服务启动时间 */
    private long startTime = 0;

    private final Map<String, String> additionalParameters = new HashMap<>();

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

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
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

    public Map<String, String> getAdditionalParameters() {
        return Collections.unmodifiableMap(additionalParameters);
    }

    public String getAdditionalParameter(String key) {
        return additionalParameters.get(key);
    }

    public void putAdditionalParameter(String key, String value) {
        additionalParameters.put(key, value);
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

    public String toUrl() {
        StringBuilder url = new StringBuilder();
        url.append(ip).append(":")
            .append(port).append("/")
            .append(service).append("?")
            .append(WEIGHT).append("=").append(weight).append("&")
            .append(WARMUP).append("=").append(warmup).append("&")
            .append(START_TIME).append("=").append(startTime);

        if (additionalParameters.size() > 0) {
            url.append("&");
            for (Map.Entry<String, String> pair : additionalParameters.entrySet()) {
                url.append(pair.getKey()).append("=").append(pair.getValue()).append("&");
            }
            url.deleteCharAt(url.length() - 1);
        }

        return url.toString();
    }

    /**
     * 通过解析 {@code url} 得到 {@code Provider}。{@code url} 的形式：{ip}:{port}/{service}?a=b&c=d
     *
     * @param url 一个包含 {@code Provider} 信息的字符串
     * @return 解析 {@code url} 后得到的 {@code Provider}
     */
    public static Provider parseProvider(String url) {
        Provider provider = new Provider();
        int l = 0, r = 0, len = url.length();

        // parse ip
        while (r < len && url.charAt(r) != ':') {
            r++;
        }
        provider.ip = url.substring(l, r);

        // parse port
        if (r < len) {
            l = ++r;
            while (r < len && url.charAt(r) != '/') {
                r++;
            }
            provider.port = Integer.parseInt(url.substring(l, r));
        }

        //parse service
        if (r < len) {
            l = ++r;
            while (r < len && url.charAt(r) != '?') {
                r++;
            }
            provider.service = url.substring(l, r);
        }

        if (provider.ip == null || provider.port == 0) {
            throw new IllegalArgumentException("Invalid provider url");
        }

        //parse addition parameter
        if (r++ < len) {
            String[] pairs = url.substring(r, len).split("&");
            for (String pair : pairs) {
                String[] keyAndValue = pair.split("=");
                if (keyAndValue.length <= 1) {
                    continue;
                }

                if (WEIGHT.equals(keyAndValue[0])) {
                    provider.setWeight(Integer.parseInt(keyAndValue[1]));
                } else if (WARMUP.equals(keyAndValue[0])) {
                    provider.setWeight(Integer.parseInt(keyAndValue[1]));
                } else if (START_TIME.equals(keyAndValue[0])) {
                    provider.setStartTime(Long.parseLong(keyAndValue[1]));
                } else {
                    provider.additionalParameters.put(keyAndValue[0], keyAndValue[1]);
                }
            }
        }

        return provider;
    }
}
