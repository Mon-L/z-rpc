package cn.zcn.rpc.remoting.utils;

import io.netty.channel.Channel;
import io.netty.util.internal.StringUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NetUtil {

    public static String getRemoteAddress(Channel channel) {
        return getRemoteHost(channel.remoteAddress()) + ":" + getRemotePort(channel.remoteAddress());
    }

    public static String getRemoteAddress(SocketAddress address) {
        return getRemoteHost(address) + ":" + getRemotePort(address);
    }

    public static String getRemoteHost(Channel channel) {
        return getRemoteHost(channel.remoteAddress());
    }

    public static String getRemoteHost(SocketAddress address) {
        if (address == null) {
            return StringUtil.EMPTY_STRING;
        }

        if (address instanceof InetSocketAddress) {
            return ((InetSocketAddress) address).getAddress().getHostAddress();
        } else {
            String addr = address.toString().trim();

            if (StringUtil.isNullOrEmpty(addr)) {
                return StringUtil.EMPTY_STRING;
            }

            if (addr.charAt(0) == '/') {
                return addr.substring(1);
            } else {
                int i = addr.indexOf('/', 1);
                if (i != -1) {
                    return addr.substring(i + 1);
                }
                return addr;
            }
        }
    }

    public static int getRemotePort(Channel channel) {
        return getRemotePort(channel.remoteAddress());
    }

    public static int getRemotePort(SocketAddress address) {
        if (address instanceof InetSocketAddress) {
            return ((InetSocketAddress) address).getPort();
        }

        return -1;
    }
}
