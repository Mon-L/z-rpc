package cn.zcn.rpc.bootstrap.utils;

import java.util.Collection;

/** @author zicung */
public class CollectionUtils {

    /**
     * 判断集合是否是空集合
     *
     * @param collection 集合
     * @return {@code true}，{@code collection} 为 {@code null} 或者 {@code collection} 的大小为零；否则，返回 {@code
     *     false}。
     */
    public static boolean isEmptyOrNull(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
