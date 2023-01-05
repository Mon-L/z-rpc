package cn.zcn.rpc.bootstrap.extension;

import java.lang.annotation.*;

/**
 * 扩展点注解，标识该类或接口的功能可以被扩展。
 *
 * @author zicung
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionPoint {
}
