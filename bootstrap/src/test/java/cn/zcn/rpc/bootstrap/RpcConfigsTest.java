package cn.zcn.rpc.bootstrap;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RpcConfigsTest {

    @Test
    public void testConfigOrder() {
        // META-INF/z-rpc/rpc-config.properties was overwritten by z-rpc/rpc-config.properties
        String name = RpcConfigs.getString("name");
        assertThat(name).isEqualTo("abc1");

        // gender is only exist in z-rpc/rpc-config.properties
        String gender = RpcConfigs.getString("gender");
        assertThat(gender).isEqualTo("male");
    }

    @Test
    public void testGetString() {
        String id = RpcConfigs.getString("id");
        assertThat(id).isEqualTo("1234");

        //test returns null if the property is not found.
        String xxx = RpcConfigs.getString("xxx");
        assertThat(xxx).isNull();
    }

    @Test
    public void testGetInteger() {
        Integer age = RpcConfigs.getInteger("age");
        assertThat(age).isEqualTo(19);

        //test returns null if the property is not found.
        Integer xxx = RpcConfigs.getInteger("xxx");
        assertThat(xxx).isNull();
    }

    @Test
    public void testGetBool() {
        Boolean isLocked = RpcConfigs.getBool("is_locked");
        assertThat(isLocked).isNotNull();
        assertThat(isLocked).isEqualTo(false);

        //test returns null if the property is not found.
        Boolean xxx = RpcConfigs.getBool("xxx");
        assertThat(xxx).isNull();
    }

    @Test
    public void testGetList() {
        //a b c
        List<String> list1 = RpcConfigs.getList("list1");
        assertThat(list1.size()).isEqualTo(3);
        assertThat(list1.get(0)).isEqualTo("a");
        assertThat(list1.get(1)).isEqualTo("b");
        assertThat(list1.get(2)).isEqualTo("c");

        List<String> list2 = RpcConfigs.getList("list2");
        assertThat(list2.size()).isEqualTo(0);

        //e f g
        List<String> list3 = RpcConfigs.getList("list3");
        assertThat(list3.size()).isEqualTo(3);
        assertThat(list3.get(0)).isEqualTo("e");
        assertThat(list3.get(1)).isEqualTo("f");
        assertThat(list3.get(2)).isEqualTo("g");
    }
}
