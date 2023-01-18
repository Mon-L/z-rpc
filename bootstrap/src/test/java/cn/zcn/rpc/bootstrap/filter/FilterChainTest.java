package cn.zcn.rpc.bootstrap.filter;

import static org.assertj.core.api.Assertions.assertThat;

import cn.zcn.rpc.bootstrap.RpcException;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Function;

/**
 * @author zicung
 */
public class FilterChainTest {

    private static StringBuilder PATH = new StringBuilder();
    private static Function<TestFilter, Boolean> RUN_CONDITION = s -> true;

    private static class TestFilter implements Filter<TestInvocation> {

        private final String name;

        private TestFilter(String name) {
            this.name = name;
        }

        @Override
        public void doFilter(TestInvocation invocation, FilterContext<TestInvocation> filterContext)
            throws RpcException {
            if (RUN_CONDITION.apply(this)) {
                PATH.append(name);
                filterContext.doFilter(invocation);
            }
        }
    }

    private static class TestInvocation extends Invocation {

        private TestInvocation() {
            super(null);
        }
    }

    @Before
    public void before() {
        resetStaticFields();
    }

    private void resetStaticFields() {
        PATH = new StringBuilder();
        RUN_CONDITION = s -> true;
    }

    @Test
    public void testConstructor() {
        FilterChain<TestInvocation> filterChain = new FilterChain<>(
            new TestFilter[] { new TestFilter("a"), new TestFilter("b"), new TestFilter("c") });

        TestInvocation invocation = new TestInvocation();
        filterChain.doFilter(invocation);
        assertThat(PATH.toString()).isEqualTo("abc");
    }

    @Test
    public void testAddFirst() {
        FilterChain<TestInvocation> filterChain = new FilterChain<>(
            new TestFilter[] { new TestFilter("a"), new TestFilter("b"), new TestFilter("c") });

        TestInvocation invocation = new TestInvocation();
        filterChain.doFilter(invocation);
        assertThat(PATH.toString()).isEqualTo("abc");

        resetStaticFields();

        //add filter
        filterChain.addFirst(new TestFilter("z"));
        invocation = new TestInvocation();
        filterChain.doFilter(invocation);
        assertThat(PATH.toString()).isEqualTo("zabc");
    }

    @Test
    public void testAddTail() {
        FilterChain<TestInvocation> filterChain = new FilterChain<>(
            new TestFilter[] { new TestFilter("a"), new TestFilter("b"), new TestFilter("c") });

        TestInvocation invocation = new TestInvocation();
        filterChain.doFilter(invocation);
        assertThat(PATH.toString()).isEqualTo("abc");

        resetStaticFields();

        //add filter
        filterChain.addLast(new TestFilter("d"));
        invocation = new TestInvocation();
        filterChain.doFilter(invocation);
        assertThat(PATH.toString()).isEqualTo("abcd");
    }

    @Test
    public void testTerminate() {
        FilterChain<TestInvocation> filterChain = new FilterChain<>(
            new TestFilter[] { new TestFilter("a"), new TestFilter("b"), new TestFilter("c"),
                               new TestFilter("d") });

        RUN_CONDITION = filter -> !filter.name.equals("c");

        TestInvocation invocation = new TestInvocation();
        filterChain.doFilter(invocation);

        //filterChain 运行到 filter "c" 时将不再继续运行
        assertThat(PATH.toString()).isEqualTo("ab");
    }
}
