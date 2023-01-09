package cn.zcn.rpc.bootstrap.consumer.filter;

import cn.zcn.rpc.bootstrap.Order;
import cn.zcn.rpc.bootstrap.extension.ExtensionLoader;
import cn.zcn.rpc.bootstrap.utils.CollectionUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 过滤器链构建者。
 *
 * <p>
 *
 * <pre>
 * 过滤器链是由一个或多个 {@code FilterChainNode} 组成的单向链表，每个 {@code FilterChainNode} 都有
 * 包含 {@code next} 属性指向下一个 {@code FilterChainNode}。如下所示：
 *  +------+   +------+   +------+   +------+
 *  |filter|   |filter|   |filter|   |filter|
 *  | next |-->| next |-->| next |-->| next |
 *  +------+   +------+   +------+   +------+
 * </pre>
 *
 * @author zicung
 */
public class FilterChainBuilder {

    private static final Comparator<Filter> FILTER_COMPARATOR = new FilterComparator();

    private FilterChainBuilder() {
    }

    /**
     * 使用 {@link ExtensionLoader} 根据过滤器名称获取过滤器并构建过滤器链。多个 {@code Filter} 使用 {@code FILTER_COMPARATOR} 进行排序。
     *
     * @param originalFilter 过滤器中的尾节点
     * @param filters 过滤器名称列表
     * @return 过滤器链的头节点
     */
    public static FilterChainNode build(Filter originalFilter, List<String> filters) {
        FilterChainNode tail = new FilterChainNode(originalFilter, null);

        if (CollectionUtils.isEmptyOrNull(filters)) {
            return tail;
        }

        List<Filter> filterRefs = new ArrayList<>();
        for (String name : filters) {
            Filter filter = ExtensionLoader.getExtensionLoader(Filter.class).getExtension(name);
            filterRefs.add(filter);
        }
        filterRefs.sort(FILTER_COMPARATOR);

        FilterChainNode head = tail;
        for (int i = filterRefs.size() - 1; i >= 0; i--) {
            Filter filter = filterRefs.get(i);
            FilterChainNode next = head;
            head = new FilterChainNode(filter, next);
        }

        return head;
    }

    /** {@code Filter} 排序器。{@link Order#value()} 的值越小排在越前面。当没有 {@code Order} 时默认值为 0。 */
    private static final class FilterComparator implements Comparator<Filter> {

        @Override
        public int compare(Filter f1, Filter f2) {
            Order order1 = f1.getClass().getAnnotation(Order.class);
            Order order2 = f2.getClass().getAnnotation(Order.class);

            return (order1 == null ? 0 : order1.value()) - (order2 == null ? 0 : order2.value());
        }
    }
}
