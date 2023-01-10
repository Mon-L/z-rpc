package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import cn.zcn.rpc.bootstrap.registry.Provider;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author zicung
 */
public class ConsistentHashLoadBalanceTest extends LoadBalanceBaseTest {

    @Test
    public void testSelectWithSameRequest() {
        List<Provider> providers = new ArrayList<>();
        providers.add(createProvider("1", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("2", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("3", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("4", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("5", 0, System.currentTimeMillis(), 2));

        int times = 10000;
        Map<Provider, Integer> counter = doSelect(times, providers, "consistentHash");

        // 仅能选中一台 provider
        assertThat(counter.size()).isEqualTo(1);
        assertThat(getSum(counter)).isEqualTo(times);
    }
}
