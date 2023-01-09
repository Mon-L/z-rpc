package cn.zcn.rpc.bootstrap;

import java.lang.annotation.*;

/** @author zicung */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
    int value() default 1;
}
