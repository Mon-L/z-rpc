package cn.zcn.rpc.bootstrap.extension;


import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Extension {
    String value();
}
