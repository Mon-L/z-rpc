package cn.zcn.rpc.bootstrap.consumer.filter;

import cn.zcn.rpc.bootstrap.Order;
import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.bootstrap.extension.Extension;
import cn.zcn.rpc.bootstrap.extension.ExtensionException;
import cn.zcn.rpc.bootstrap.registry.Provider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FilterChainBuilderTest {

    private static final List<String> SEQUENCE = new ArrayList<>();

    @Extension("filter1")
    public static class Filter1 implements Filter {
        @Override
        public RpcResponse doFilter(Provider provider, RpcRequest request, FilterChainNode nextFilter) throws RpcException {
            SEQUENCE.add("filter1");
            return nextFilter.invoke(provider, request);
        }
    }

    @Order(20)
    @Extension("filter2")
    public static class Filter2 implements Filter {
        @Override
        public RpcResponse doFilter(Provider provider, RpcRequest request, FilterChainNode nextFilter) throws RpcException {
            SEQUENCE.add("filter2");
            return nextFilter.invoke(provider, request);
        }
    }

    @Order(25)
    @Extension("suspendFilter")
    public static class SuspendFilter implements Filter {
        @Override
        public RpcResponse doFilter(Provider provider, RpcRequest request, FilterChainNode nextFilter) throws RpcException {
            RpcResponse resp = new RpcResponse();
            resp.set(false);
            return resp;
        }
    }

    @Order(30)
    @Extension("filter3")
    public static class Filter3 implements Filter {
        @Override
        public RpcResponse doFilter(Provider provider, RpcRequest request, FilterChainNode nextFilter) throws RpcException {
            SEQUENCE.add("filter3");
            return nextFilter.invoke(provider, request);
        }
    }

    @Before
    public void beforeEach() {
        SEQUENCE.clear();
    }

    @Test
    public void testFilterOrder() throws ExecutionException {
        List<String> filters = new ArrayList<String>() {{
            add("filter3");
            add("filter1");
            add("filter2");
        }};

        Filter filter = (provider, request, next) -> {
            RpcResponse resp = new RpcResponse();
            resp.set(true);
            return resp;
        };

        FilterChainNode filterChainNode = FilterChainBuilder.build(filter, filters);
        RpcResponse response = filterChainNode.invoke(Mockito.mock(Provider.class), Mockito.mock(RpcRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.get()).isEqualTo(true);
        assertThat(SEQUENCE.get(0)).isEqualTo("filter1");
        assertThat(SEQUENCE.get(1)).isEqualTo("filter2");
        assertThat(SEQUENCE.get(2)).isEqualTo("filter3");
    }

    @Test
    public void testInvalidFilter() {
        List<String> filters = new ArrayList<String>() {{
            add("filter6");
        }};

        assertThatThrownBy(() -> FilterChainBuilder.build((provider, request, next) -> null, filters)).isInstanceOf(ExtensionException.class);
    }

    @Test
    public void testFilterChainButSuspend() throws ExecutionException {
        List<String> filters = new ArrayList<String>() {{
            add("filter1");
            add("filter3");
            add("suspendFilter");
            add("filter2");
        }};

        Filter filter = (provider, request, next) -> {
            RpcResponse resp = new RpcResponse();
            resp.set(true);
            return resp;
        };

        /*
         * 排序后 filter 的顺序：filter1 -> filter2 -> suspendFilter -> filter3
         * filter chain 的运行将在 suspendFilter 终止
         */
        FilterChainNode filterChainNode = FilterChainBuilder.build(filter, filters);
        RpcResponse response = filterChainNode.invoke(Mockito.mock(Provider.class), Mockito.mock(RpcRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.get()).isEqualTo(false);
        assertThat(SEQUENCE.size()).isEqualTo(2);
        assertThat(SEQUENCE.get(0)).isEqualTo("filter1");
        assertThat(SEQUENCE.get(1)).isEqualTo("filter2");
    }
}
