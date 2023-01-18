package cn.zcn.rpc.remoting.utils;

import org.junit.Test;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author zicung
 */
public class PropertiesUtilsTest {

    @Test
    public void testGetString() {
        Properties properties = new Properties();
        properties.put("id", "1234");

        String id = PropertiesUtils.getString(properties, "id");
        assertThat(id).isEqualTo("1234");

        // test returns null if the property is not found.
        String xxx = PropertiesUtils.getString(properties, "xxx");
        assertThat(xxx).isNull();
    }

    @Test
    public void testGetInteger() {
        Properties properties = new Properties();
        properties.put("age", 19);

        Integer age = PropertiesUtils.getInteger(properties, "age");
        assertThat(age).isEqualTo(19);

        // test returns null if the property is not found.
        Integer xxx = PropertiesUtils.getInteger(properties, "xxx");
        assertThat(xxx).isNull();
    }

    @Test
    public void testGetBool() {
        Properties properties = new Properties();
        properties.put("is_locked", false);

        Boolean isLocked = PropertiesUtils.getBool(properties, "is_locked");
        assertThat(isLocked).isNotNull();
        assertThat(isLocked).isEqualTo(false);

        // test returns null if the property is not found.
        Boolean xxx = PropertiesUtils.getBool(properties, "xxx");
        assertThat(xxx).isNull();
    }

    @Test
    public void testGetList() {
        Properties properties = new Properties();
        properties.put("list1", "a b c");
        properties.put("list2", " ");
        properties.put("list3", " e f g ");

        // a b c
        List<String> list1 = PropertiesUtils.getList(properties, "list1");
        assertThat(list1.size()).isEqualTo(3);
        assertThat(list1.get(0)).isEqualTo("a");
        assertThat(list1.get(1)).isEqualTo("b");
        assertThat(list1.get(2)).isEqualTo("c");

        List<String> list2 = PropertiesUtils.getList(properties, "list2");
        assertThat(list2.size()).isEqualTo(0);

        // e f g
        List<String> list3 = PropertiesUtils.getList(properties, "list3");
        assertThat(list3.size()).isEqualTo(3);
        assertThat(list3.get(0)).isEqualTo("e");
        assertThat(list3.get(1)).isEqualTo("f");
        assertThat(list3.get(2)).isEqualTo("g");
    }
}
