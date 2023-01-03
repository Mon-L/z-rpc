package cn.zcn.rpc.bootstrap.utils;


import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(signatures.size()).isEqualTo(3);
        assertThat(signatures.get(0)).isEqualTo("addUser");
        assertThat(signatures.get(1)).isEqualTo("addUser:java.lang.String");
        assertThat(signatures.get(2)).isEqualTo("addUser:java.lang.String,int,boolean,java.util.concurrent.Callable");
    }

    @Test
    public void testGetMethodSignature3() {
        assertThat(MethodSignatureUtil.getMethodSignature("addUser", null)).isEqualTo("addUser");
        assertThat(MethodSignatureUtil.getMethodSignature("addUser", new String[]{})).isEqualTo("addUser");
        assertThat(
                MethodSignatureUtil.getMethodSignature("addUser", new String[]{"java.lang.String", "int", "boolean"})).isEqualTo("addUser:java.lang.String,int,boolean");
    }
}
