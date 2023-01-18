package cn.zcn.rpc.bootstrap.filter;

import static org.assertj.core.api.Assertions.assertThat;

import cn.zcn.rpc.bootstrap.Order;
import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.extension.Extension;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zicung
 */
public class FilterChainBuilderTest {

    private static int COUNT = 0;
    private static StringBuilder PATH = new StringBuilder();

    private static class TestConsumerFilter implements ConsumerFilter {
        private final String name;

        public TestConsumerFilter(String name) {
            this.name = name;
        }

        @Override
        public void doFilter(ConsumerInvocation invocation, FilterContext<ConsumerInvocation> filterContext)
            throws RpcException {
            COUNT++;
            PATH.append(name);
            filterContext.doFilter(invocation);
        }
    }

    @Order(1)
    @Extension("cf1")
    public static class TestConsumerFilter1 extends TestConsumerFilter {

        public TestConsumerFilter1() {
            super("1");
        }
    }

    @Order(3)
    @Extension("cf2")
    public static class TestConsumerFilter2 extends TestConsumerFilter {

        public TestConsumerFilter2() {
            super("2");
        }
    }

    @Order(2)
    @Extension("cf3")
    public static class TestConsumerFilter3 extends TestConsumerFilter {

        public TestConsumerFilter3() {
            super("3");
        }
    }

    @Extension("pf1")
    public static class TestProviderFilter implements ProviderFilter {

        @Override
        public void doFilter(ProviderInvocation invocation, FilterContext<ProviderInvocation> filterContext)
            throws RpcException {
            COUNT++;
            PATH.append("1");
            filterContext.doFilter(invocation);
        }
    }

    @Before
    public void before() {
        COUNT = 0;
        PATH = new StringBuilder();
    }

    @Test
    public void testBuildConsumerFilter() {
        List<String> filters = new ArrayList<>();
        filters.add("cf2");
        filters.add("cf3");
        filters.add("cf1");

        FilterChain<ConsumerInvocation> filterChain = FilterChainBuilder.buildConsumerFilterChain(filters);
        filterChain.doFilter(new ConsumerInvocation(new RpcRequest()));

        assertThat(COUNT).isEqualTo(3);
    }

    @Test
    public void testConsumerFilterOrder() {
        List<String> filters = new ArrayList<>();
        filters.add("cf2");
        filters.add("cf3");
        filters.add("cf1");

        FilterChain<ConsumerInvocation> filterChain = FilterChainBuilder.buildConsumerFilterChain(filters);
        filterChain.doFilter(new ConsumerInvocation(new RpcRequest()));

        assertThat(COUNT).isEqualTo(3);
        assertThat(PATH.toString()).isEqualTo("132");
    }

    @Test
    public void testBuildProviderFilter() {
        List<String> filters = new ArrayList<>();
        filters.add("pf1");

        FilterChain<ProviderInvocation> filterChain = FilterChainBuilder.buildProviderFilterChain(filters);
        filterChain.doFilter(new ProviderInvocation(new RpcRequest()));

        assertThat(COUNT).isEqualTo(1);
        assertThat(PATH.toString()).isEqualTo("1");
    }
}
