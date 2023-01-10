package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import static org.assertj.core.api.Assertions.assertThat;

import cn.zcn.rpc.bootstrap.registry.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class RoundRobinLoadBalanceTest extends LoadBalanceBaseTest {

    @Test
    public void testSelectWithSameWeight() {
        List<Provider> providers = new ArrayList<>();
        providers.add(createProvider("1", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("2", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("3", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("4", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("5", 0, System.currentTimeMillis(), 2));

        int times = 10000;
        Map<Provider, Integer> counter = doSelect(times, providers, "roundRobin");
        assertThat(counter.size()).isEqualTo(providers.size());
        assertThat(getSum(counter)).isEqualTo(times);

        // 1 : 1 : 1 : 1 : 1
        for (Provider provider : providers) {
            int countPerProvider = counter.get(provider);
            assertThat(countPerProvider).isEqualTo(times / providers.size());
        }
    }

    @Test
    public void testSelectWithDiffWeight() {
        List<Provider> providers = new ArrayList<>();
        providers.add(createProvider("1", 0, System.currentTimeMillis(), 1));
        providers.add(createProvider("2", 0, System.currentTimeMillis(), 3));
        providers.add(createProvider("3", 0, System.currentTimeMillis(), 6));

        int times = 10000;
        Map<Provider, Integer> counter = doSelect(times, providers, "roundRobin");
        assertThat(counter.size()).isEqualTo(providers.size());
        assertThat(getSum(counter)).isEqualTo(times);

        // 1 : 3 : 6
        assertThat(counter.get(providers.get(0))).isEqualTo(1000);
        assertThat(counter.get(providers.get(1))).isEqualTo(3000);
        assertThat(counter.get(providers.get(2))).isEqualTo(6000);
    }

    @Test
    public void testSelectWithZeroWeight() {
        List<Provider> providers = new ArrayList<>();
        providers.add(createProvider("1", 0, System.currentTimeMillis(), 1));
        providers.add(createProvider("2", 0, System.currentTimeMillis(), 0));
        providers.add(createProvider("3", 0, System.currentTimeMillis(), 1));

        int times = 1000;
        Map<Provider, Integer> counter = doSelect(times, providers, "roundRobin");

        assertThat(getSum(counter)).isEqualTo(times);

        // 1 : 0 : 1
        assertThat(counter.get(providers.get(0))).isEqualTo(500);
        assertThat(counter.get(providers.get(1))).isNull();
        assertThat(counter.get(providers.get(2))).isEqualTo(500);
    }

    @Test
    public void testSelectWithWarmup() throws InterruptedException {
        List<Provider> providers = new ArrayList<>();
        providers.add(createProvider("1", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("2", 2000, System.currentTimeMillis(), 4));
        providers.add(createProvider("3", 0, System.currentTimeMillis(), 4));

        TimeUnit.MILLISECONDS.sleep(1000);

        int times = 1000;
        Map<Provider, Integer> counter = doSelect(times, providers, "roundRobin");

        assertThat(counter.size()).isEqualTo(providers.size());
        assertThat(getSum(counter)).isEqualTo(times);

        // 1 : 1 : 2
        assertThat(counter.get(providers.get(0))).isEqualTo(250);
        assertThat(counter.get(providers.get(1))).isEqualTo(250);
        assertThat(counter.get(providers.get(2))).isEqualTo(500);
    }
}
