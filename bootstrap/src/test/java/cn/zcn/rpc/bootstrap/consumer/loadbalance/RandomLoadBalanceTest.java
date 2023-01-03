package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import cn.zcn.rpc.bootstrap.registry.Provider;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class RandomLoadBalanceTest extends LoadBalanceBaseTest {

    @Test
    public void testSelectWithSameWeight() {
        List<Provider> providers = new ArrayList<>();
        providers.add(createProvider("1", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("2", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("3", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("4", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("5", 0, System.currentTimeMillis(), 2));

        int times = 10000;
        Map<Provider, Integer> counter = doSelect(times, providers, "random");
        assertThat(counter.size()).isEqualTo(providers.size());
        assertThat(getSum(counter)).isEqualTo(times);

        //1 : 1 : 1 : 1 : 1
        for (Provider provider : providers) {
            int countPerProvider = counter.get(provider);
            assertThat(((times - countPerProvider) / providers.size()) < (times / providers.size())).isTrue();

            System.out.println(countPerProvider);
        }
    }

    @Test
    public void testSelectWithDiffWeight() {
        List<Provider> providers = new ArrayList<>();
        providers.add(createProvider("1", 0, System.currentTimeMillis(), 1));
        providers.add(createProvider("2", 0, System.currentTimeMillis(), 3));
        providers.add(createProvider("3", 0, System.currentTimeMillis(), 6));

        int times = 10000;
        Map<Provider, Integer> counter = doSelect(times, providers, "random");
        assertThat(counter.size()).isEqualTo(providers.size());
        assertThat(getSum(counter)).isEqualTo(times);

        //1 : 3 : 6
        for (Provider provider : providers) {
            int countPerProvider = counter.get(provider);
            assertThat(((times - countPerProvider) / 10) < (times / 10)).isTrue();

            System.out.println(countPerProvider);
        }
    }

    @Test
    public void testSelectWithZeroWeight() {
        List<Provider> providers = new ArrayList<>();
        providers.add(createProvider("1", 0, System.currentTimeMillis(), 1));
        providers.add(createProvider("2", 0, System.currentTimeMillis(), 0));
        providers.add(createProvider("3", 0, System.currentTimeMillis(), 1));

        int times = 1000;
        Map<Provider, Integer> counter = doSelect(times, providers, "random");

        //权重为零的节点无法被选中
        assertThat(counter.size()).isEqualTo(providers.size() - 1);
        assertThat(getSum(counter)).isEqualTo(times);
        assertThat(counter.get(providers.get(1))).isNull();

        //1 : 0 : 1
        for (Provider provider : providers) {
            Integer countPerProvider = counter.get(provider);
            System.out.println(countPerProvider == null ? 0 : countPerProvider);
        }
    }

    @Test
    public void testSelectWithWarmup() throws InterruptedException {
        List<Provider> providers = new ArrayList<>();
        providers.add(createProvider("1", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("2", 2000, System.currentTimeMillis(), 4));
        providers.add(createProvider("3", 0, System.currentTimeMillis(), 4));

        TimeUnit.MILLISECONDS.sleep(1000);

        int times = 1000;
        Map<Provider, Integer> counter = doSelect(times, providers, "random");

        assertThat(counter.size()).isEqualTo(providers.size());
        assertThat(getSum(counter)).isEqualTo(times);

        //1 : 1 : 2
        for (Provider provider : providers) {
            int countPerProvider = counter.get(provider);

            System.out.println(countPerProvider);
        }
    }
}
