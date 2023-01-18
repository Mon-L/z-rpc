package cn.zcn.rpc.bootstrap.filter;

import cn.zcn.rpc.bootstrap.RpcException;

/**
 * 由一个或多个 {@link Filter}s 组成的过滤器链。让用户可以在请求调用过程中处理 {@code RpcRequest}。
 * <h3>请求流动</h3>
 * 每个请求在执行前都会在 filterChain 中流动。下图展示了请求的流动过程，可以使用 {@link FilterContext} 控制请求在 filterChain
 * 的流动。
 *
 * <pre>
 *                +--------+     +--------+     +--------+     +----------+     +--------+    +--------+
 * RpcRequest --> |  head  | --> |filter 1| --> |filter 2| --> |filter N-1| --> |filter N| -->|  tail  |
 *                +--------+     +--------+     +--------+     +----------+     +--------+    +--------+
 * </pre>
 *
 * <h3>请求控制</h3>
 * 下面例子展示了如何控制请求的流动：
 * <pre>
 *  public class MyInboundHandler implement {@link Filter<Invocation>} {
 *      {@code @Override}
 *      public void doFilter(Invocation invocation, FilterContext filterContext) throws RpcException{
 *          System.out.println("doFilter ..");
 *
 *          //调用下一个filter
 *          filterContext.doFilter(invocation);
 *      }
 *  }
 *
 * @author zicung
 */
public class FilterChain<T extends Invocation> {

    private final AbstractFilterContext<T> head;
    private final AbstractFilterContext<T> tail;

    public FilterChain(Filter<T>[] filters) {
        head = new HeadFilterContext<>();
        tail = new TailFilterContext<>();
        head.next = tail;
        tail.prev = head;

        for (Filter<T> filter : filters) {
            addLast(filter);
        }
    }

    /**
     * 将 filter 添加到 filter chain 的尾部
     *
     * @param filter 待添加的filter
     */
    public void addLast(Filter<T> filter) {
        AbstractFilterContext<T> cur = new DefaultFilterContext<>(filter);
        cur.prev = tail.prev;
        cur.next = tail;
        tail.prev.next = cur;
        tail.prev = cur;
    }

    /**
     * 将 filter 添加到 filter chain 的头部
     *
     * @param filter 待添加的filter
     */
    public void addFirst(Filter<T> filter) {
        AbstractFilterContext<T> cur = new DefaultFilterContext<>(filter);
        cur.prev = head;
        cur.next = head.next;
        head.next.prev = cur;
        head.next = cur;
    }

    /**
     * 执行过滤器链
     *
     * @param invocation 当前调用请求
     */
    public void doFilter(T invocation) {
        head.doFilter(invocation);
    }

    private static abstract class AbstractFilterContext<T extends Invocation> implements FilterContext<T>, Filter<T> {
        private AbstractFilterContext<T> next;
        private AbstractFilterContext<T> prev;

        @Override
        public void doFilter(T invocation) throws RpcException {
            next.filter().doFilter(invocation, next);
        }

        @Override
        public Filter<T> filter() {
            return this;
        }
    }

    private static class HeadFilterContext<T extends Invocation> extends AbstractFilterContext<T> {

        @Override
        public void doFilter(T invocation, FilterContext<T> filterContext) {
            filterContext.doFilter(invocation);
        }

        @Override
        public Filter<T> filter() {
            return this;
        }
    }

    private static class TailFilterContext<T extends Invocation> extends AbstractFilterContext<T> {

        @Override
        public void doFilter(T invocation, FilterContext<T> filterContext) {
        }

        @Override
        public Filter<T> filter() {
            return this;
        }
    }

    private static class DefaultFilterContext<T extends Invocation> extends AbstractFilterContext<T> {

        private final Filter<T> filter;

        public DefaultFilterContext(Filter<T> filter) {
            this.filter = filter;
        }

        @Override
        public void doFilter(T invocation, FilterContext<T> filterContext) {
            filter().doFilter(invocation, filterContext);
        }

        @Override
        public Filter<T> filter() {
            return filter;
        }
    }
}
