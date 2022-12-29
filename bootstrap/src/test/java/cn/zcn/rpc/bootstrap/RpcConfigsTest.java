package cn.zcn.rpc.bootstrap;


import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    public void testGetList(){
        //a b c
        List<String> list1 = RpcConfigs.getList("list1");
        assertEquals(3, list1.size());
        assertEquals("a", list1.get(0));
        assertEquals("b", list1.get(1));
        assertEquals("c", list1.get(2));

        List<String> list2 = RpcConfigs.getList("list2");
        assertEquals(0, list2.size());

        //e f g
        List<String> list3 = RpcConfigs.getList("list3");
        assertEquals(3, list3.size());
        assertEquals("e", list3.get(0));
        assertEquals("f", list3.get(1));
        assertEquals("g", list3.get(2));
    }
}
