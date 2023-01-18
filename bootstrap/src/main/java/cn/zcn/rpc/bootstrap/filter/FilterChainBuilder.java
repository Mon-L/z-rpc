package cn.zcn.rpc.bootstrap.filter;

import cn.zcn.rpc.bootstrap.Order;
import cn.zcn.rpc.bootstrap.extension.ExtensionLoader;
import cn.zcn.rpc.bootstrap.utils.CollectionUtils;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 过滤器链构建者。根据过滤器名称列表构建 {@link FilterChain}，使用 {@code FILTER_COMPARATOR} 对过滤器进行排序。
 * </p>
 * 使用 {@link ExtensionLoader} 加载过滤器实例。
 *
 * @author zicung
 */
public class FilterChainBuilder {

    private static final Comparator<Filter<? extends Invocation>> FILTER_COMPARATOR = new FilterComparator();

    private FilterChainBuilder() {
    }

    /**
     * 构建服务消费者过滤器链。
     *
     * @param filters 过滤器列表
     * @return 过滤器链
     */
    public static FilterChain<ConsumerInvocation> buildConsumerFilterChain(List<String> filters) {
        return buildFilterChain(filters, ConsumerFilter.class);
    }

    /**
     * 构建服务提供者过滤器链。
     *
     * @param filters 过滤器列表
     * @return 过滤器链
     */
    public static FilterChain<ProviderInvocation> buildProviderFilterChain(List<String> filters) {
        return buildFilterChain(filters, ProviderFilter.class);
    }

    @SuppressWarnings({ "unchecked" })
    public static <T extends Invocation> FilterChain<T> buildFilterChain(List<String> filters,
                                                                         Class<? extends Filter<T>> filterClass) {
        if (CollectionUtils.isEmptyOrNull(filters)) {
            return new FilterChain<T>(new Filter[0]);
        }

        Filter<T>[] filterRefs = new Filter[filters.size()];
        for (int i = 0; i < filters.size(); i++) {
            String name = filters.get(i);
            Filter<T> filter = ExtensionLoader.getExtensionLoader(filterClass).getExtension(name);

            filterRefs[i] = filter;
        }
        Arrays.sort(filterRefs, FILTER_COMPARATOR);

        return new FilterChain<>(filterRefs);
    }

    /** {@code Filter} 排序器。{@link Order#value()} 的值越小排在越前面。当没有 {@code Order} 时默认值为 0。 */
    private static final class FilterComparator implements Comparator<Filter<? extends Invocation>> {

        @Override
        public int compare(Filter f1, Filter f2) {
            Order order1 = f1.getClass().getAnnotation(Order.class);
            Order order2 = f2.getClass().getAnnotation(Order.class);

            return (order1 == null ? 0 : order1.value()) - (order2 == null ? 0 : order2.value());
        }
    }
}
