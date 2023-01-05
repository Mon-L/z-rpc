package cn.zcn.rpc.bootstrap.extension;

import java.lang.annotation.*;

/**
 * 扩展注解，表示该类扩展了某个带有 {@link ExtensionPoint} 注解的类的功能。
 *
 * @author zicung
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Extension {
    String value();
}
