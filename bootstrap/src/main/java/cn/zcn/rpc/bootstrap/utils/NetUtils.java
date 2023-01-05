package cn.zcn.rpc.bootstrap.utils;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;

/**
 * IP and Port Helper for RPC
 *
 * @author dubbo
 */
public final class NetUtils {

    private NetUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);

    /**
     * returned port range is [30000, 39999]
     */
    private static final int RND_PORT_START = 30000;
    private static final int RND_PORT_RANGE = 10000;

    /**
     * valid port range is (0, 65535]
     */
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    private static volatile InetAddress LOCAL_ADDRESS = null;
    private static volatile Inet6Address LOCAL_ADDRESS_V6 = null;

    private static final String LOCAL_HOST_VALUE = "127.0.0.1";
    private static final String ANY_HOST_VALUE = "0.0.0.0";

    private static final String PERCENT = "%";
    private static final char PERCENT_CHAR = '%';

    /**
     * store the used port.
     * the set used only on the synchronized method.
     */
    private static final BitSet USED_PORT = new BitSet(65536);

    private static int getRandomPort() {
        return RND_PORT_START + ThreadLocalRandom.current().nextInt(RND_PORT_RANGE);
    }

    public static synchronized int getAvailablePort() {
        int randomPort = getRandomPort();
        return getAvailablePort(randomPort);
    }

    public static synchronized int getAvailablePort(int port) {
        if (port < MIN_PORT) {
            return MIN_PORT;
        }

        for (int i = port; i < MAX_PORT; i++) {
            if (USED_PORT.get(i)) {
                continue;
            }
            try (ServerSocket ignored = new ServerSocket(i)) {
                USED_PORT.set(i);
                port = i;
                break;
            } catch (IOException e) {
                // continue
            }
        }
        return port;
    }

    private static boolean isValidV4Address(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }

        String name = address.getHostAddress();
        return (name != null
                && IP_PATTERN.matcher(name).matches()
                && !ANY_HOST_VALUE.equals(name)
                && !LOCAL_HOST_VALUE.equals(name));
    }

    /**
     * Check if an ipv6 address
     *
     * @return true if it is reachable
     */
    private static boolean isPreferIPV6Address() {
        return Boolean.getBoolean("java.net.preferIPv6Addresses");
    }

    /**
     * normalize the ipv6 Address, convert scope name to scope id.
     * e.g.
     * convert
     * fe80:0:0:0:894:aeec:f37d:23e1%en0
     * to
     * fe80:0:0:0:894:aeec:f37d:23e1%5
     * <p>
     * The %5 after ipv6 address is called scope id.
     * see java doc of {@link Inet6Address} for more details.
     *
     * @param address the input address
     * @return the normalized address, with scope id converted to int
     */
    private static InetAddress normalizeV6Address(Inet6Address address) {
        String addr = address.getHostAddress();
        int i = addr.lastIndexOf(PERCENT_CHAR);
        if (i > 0) {
            try {
                return InetAddress.getByName(addr.substring(0, i) + PERCENT_CHAR + address.getScopeId());
            } catch (UnknownHostException e) {
                // ignore
                logger.debug("Unknown IPV6 address: ", e);
            }
        }
        return address;
    }

    private static volatile String HOST_ADDRESS;

    public static String getLocalHost() {
        if (HOST_ADDRESS != null) {
            return HOST_ADDRESS;
        }

        InetAddress address = getLocalAddress();
        if (address != null) {
            if (address instanceof Inet6Address) {
                String ipv6AddressString = address.getHostAddress();
                if (ipv6AddressString.contains(PERCENT)) {
                    ipv6AddressString = ipv6AddressString.substring(0, ipv6AddressString.indexOf(PERCENT));
                }
                HOST_ADDRESS = ipv6AddressString;
                return HOST_ADDRESS;
            }

            HOST_ADDRESS = address.getHostAddress();
            return HOST_ADDRESS;
        }

        return LOCAL_HOST_VALUE;
    }

    /**
     * Find first valid IP from local network card
     *
     * @return first valid local IP
     */
    private static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }
        InetAddress localAddress = getLocalAddress0();
        LOCAL_ADDRESS = localAddress;
        return localAddress;
    }

    private static Inet6Address getLocalAddressV6() {
        if (LOCAL_ADDRESS_V6 != null) {
            return LOCAL_ADDRESS_V6;
        }
        Inet6Address localAddress = getLocalAddress0V6();
        LOCAL_ADDRESS_V6 = localAddress;
        return localAddress;
    }

    private static Optional<InetAddress> toValidAddress(InetAddress address) {
        if (address instanceof Inet6Address) {
            Inet6Address v6Address = (Inet6Address) address;
            if (isPreferIPV6Address()) {
                return Optional.ofNullable(normalizeV6Address(v6Address));
            }
        }
        if (isValidV4Address(address)) {
            return Optional.of(address);
        }
        return Optional.empty();
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress;

        try {
            NetworkInterface networkInterface = findNetworkInterface();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                Optional<InetAddress> addressOp = toValidAddress(addresses.nextElement());
                if (addressOp.isPresent()) {
                    try {
                        if (addressOp.get().isReachable(100)) {
                            return addressOp.get();
                        }
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage());
        }

        try {
            localAddress = InetAddress.getLocalHost();
            Optional<InetAddress> addressOp = toValidAddress(localAddress);
            if (addressOp.isPresent()) {
                return addressOp.get();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage());
        }

        localAddress = getLocalAddressV6();

        return localAddress;
    }

    private static Inet6Address getLocalAddress0V6() {
        try {
            NetworkInterface networkInterface = findNetworkInterface();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address instanceof Inet6Address) {
                    if (!address.isLoopbackAddress() //filter 127.x.x.x
                            && !address.isAnyLocalAddress() // filter 0.0.0.0
                            && !address.isLinkLocalAddress() //filter 169.254.0.0/16
                            && address.getHostAddress().contains(":")) {//filter IPv6
                        return (Inet6Address) address;
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage());
        }

        return null;
    }

    /**
     * Get the valid {@link NetworkInterface network interfaces}
     *
     * @return the valid {@link NetworkInterface}s
     * @throws SocketException SocketException if an I/O error occurs.
     * @since 2.7.6
     */
    private static List<NetworkInterface> getValidNetworkInterfaces() throws SocketException {
        List<NetworkInterface> validNetworkInterfaces = new LinkedList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            validNetworkInterfaces.add(interfaces.nextElement());
        }
        return validNetworkInterfaces;
    }

    /**
     * Get the suitable {@link NetworkInterface}
     *
     * @return If no {@link NetworkInterface} is available , return <code>null</code>
     * @since 2.7.6
     */
    private static NetworkInterface findNetworkInterface() {
        List<NetworkInterface> validNetworkInterfaces = emptyList();
        try {
            validNetworkInterfaces = getValidNetworkInterfaces();
        } catch (Throwable e) {
            logger.warn(e.getMessage());
        }

        for (NetworkInterface networkInterface : validNetworkInterfaces) {
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                Optional<InetAddress> addressOp = toValidAddress(addresses.nextElement());
                if (addressOp.isPresent()) {
                    try {
                        if (addressOp.get().isReachable(100)) {
                            return networkInterface;
                        }
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }

        return null;
    }
}
