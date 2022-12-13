package cn.zcn.rpc.bootstrap;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RpcConfigsTest {

    @Test
    public void testConfigOrder() {
        // META-INF/z-rpc/rpc-config.properties was overwritten by z-rpc/rpc-config.properties
        String name = RpcConfigs.getString("name");
        assertEquals("abc1", name);

        // gender is only exist in z-rpc/rpc-config.properties
        String gender = RpcConfigs.getString("gender");
        assertEquals("male", gender);
    }

    @Test
    public void testGetString() {
        String id = RpcConfigs.getString("id");
        assertEquals("1234", id);

        //test returns null if the property is not found.
        String xxx = RpcConfigs.getString("xxx");
        assertNull(xxx);
    }

    @Test
    public void testGetInteger() {
        Integer age = RpcConfigs.getInteger("age");
        assertEquals(19, age);

        //test returns null if the property is not found.
        Integer xxx = RpcConfigs.getInteger("xxx");
        assertNull(xxx);
    }

    @Test
    public void testGetBool() {
        Boolean isLocked = RpcConfigs.getBool("is_locked");
        assertNotNull(isLocked);
        assertFalse(isLocked);

        //test returns null if the property is not found.
        Boolean xxx = RpcConfigs.getBool("xxx");
        assertNull(xxx);
    }
}
