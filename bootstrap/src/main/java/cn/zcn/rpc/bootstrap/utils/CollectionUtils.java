package cn.zcn.rpc.bootstrap.utils;

import java.util.Collection;

public class CollectionUtils {
    public static boolean isEmptyOrNull(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
