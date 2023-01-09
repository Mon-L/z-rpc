package cn.zcn.rpc.remoting;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * remoting url
 *
 * @author zicung
 */
public class Url {
    /** 地址 */
    private final SocketAddress address;

    /** 最大连接数，指该 Url 可以建立多少条连接 */
    private int maxConnectionNum;

    private Url(SocketAddress address) {
        this.address = address;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public int getMaxConnectionNum() {
        return maxConnectionNum;
    }

    @Override
    public String toString() {
        return this.address.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Url)) {
            return false;
        }

        Url url = (Url) o;

        return Objects.equals(address, url.address);
    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }

    public static final class Builder {
        private final SocketAddress address;
        private int maxConnectionNum = 1;

        public Builder(SocketAddress address) {
            this.address = address;
        }

        public Builder maxConnectionNum(int maxConnectionNum) {
            this.maxConnectionNum = maxConnectionNum;
            return this;
        }

        public Url build() {
            Url url = new Url(address);
            url.maxConnectionNum = maxConnectionNum;
            return url;
        }
    }
}
