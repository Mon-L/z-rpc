package cn.zcn.rpc.bootstrap.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MethodSignatureUtilTest {

    private interface TestInterface {
        void addUser();

        void addUser(String name);

        void addUser(String name, int age, boolean locked, Callable<Object> callable);
    }

    @Test
    public void testGetMethodSignature1() {
        List<String> signatures = new ArrayList<>();
        for (Method m : TestInterface.class.getMethods()) {
            signatures.add(MethodSignatureUtil.getMethodSignature(m));
        }

        assertEquals(3, signatures.size());
        assertEquals("addUser", signatures.get(0));
        assertEquals("addUser:java.lang.String", signatures.get(1));
        assertEquals("addUser:java.lang.String,int,boolean,java.util.concurrent.Callable", signatures.get(2));
    }

    @Test
    public void testGetMethodSignature3() {
        assertEquals("addUser", MethodSignatureUtil.getMethodSignature("addUser", null));
        assertEquals("addUser", MethodSignatureUtil.getMethodSignature("addUser", new String[]{}));
        assertEquals("addUser:java.lang.String,int,boolean",
                MethodSignatureUtil.getMethodSignature("addUser", new String[]{"java.lang.String", "int", "boolean"}));
    }
}
