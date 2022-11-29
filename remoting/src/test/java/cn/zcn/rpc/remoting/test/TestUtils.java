package cn.zcn.rpc.remoting.test;


import io.netty.util.internal.ThreadLocalRandom;

public class TestUtils {

    private static final String LOCAL_ADDR_ID = "test.addr.id";

    public static String getLocalAddressId() {
        return LOCAL_ADDR_ID + ThreadLocalRandom.current().nextInt();
    }
}
