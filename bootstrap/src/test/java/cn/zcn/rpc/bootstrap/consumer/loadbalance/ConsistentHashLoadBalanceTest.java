package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.registry.Provider;
import org.assertj.core.data.Offset;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;

/**
 * @author zicung
 */
public class ConsistentHashLoadBalanceTest extends LoadBalanceBaseTest {

    @Test
    public void testSelectWithSameWeight() {
        List<Provider> providers = new ArrayList<>();
        providers.add(createProvider("1", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("2", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("3", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("4", 0, System.currentTimeMillis(), 2));
        providers.add(createProvider("5", 0, System.currentTimeMillis(), 2));

        int times = 10000;
        Map<Provider, Integer> counter = doSelect(times, providers, "consistentHash");

        // 总是选中同一个 provider
        assertThat(counter.size()).isEqualTo(1);
        assertThat(getSum(counter)).isEqualTo(times);
    }

    @Test
    public void testSelectWithDiffWeight() {
        List<Provider> providers = new ArrayList<>();
        providers.add(createProvider("1", 0, System.currentTimeMillis(), 1));
        providers.add(createProvider("2", 0, System.currentTimeMillis(), 1));
        providers.add(createProvider("3", 0, System.currentTimeMillis(), 6));
        providers.add(createProvider("4", 0, System.currentTimeMillis(), 1));
        providers.add(createProvider("5", 0, System.currentTimeMillis(), 1));

        int times = 10000;
        Map<Provider, Integer> counters = new HashMap<>();
        for (int i = 0; i < times; i++) {
            RpcRequest request = new RpcRequest();
            request.setClazz("cn.zcn.rpc.Example");
            request.setMethodName("method");
            request.setParameterTypes(new String[] { String.class.getName() });
            request.setParameters(new Object[] { "ziugxif" + ThreadLocalRandom.current().nextInt(1000000) + "foo" });

            Map<Provider, Integer> counter = doSelectWithRequest(1, providers, "consistentHash", request);
            Provider selected = counter.keySet().iterator().next();
            counters.put(selected, counters.getOrDefault(selected, 0) + counter.get(selected));
        }

        // 1:1:6:1:1
        assertThat(getSum(counters)).isEqualTo(times);
        assertThat(counters.get(providers.get(0))).isCloseTo(1000, Offset.offset(200));
        assertThat(counters.get(providers.get(1))).isCloseTo(1000, Offset.offset(200));
        assertThat(counters.get(providers.get(2))).isCloseTo(6000, Offset.offset(200));
        assertThat(counters.get(providers.get(3))).isCloseTo(1000, Offset.offset(200));
        assertThat(counters.get(providers.get(4))).isCloseTo(1000, Offset.offset(200));
    }

    @Test
    public void testSelectWhenRemoveProvider() {
        //创建节点
        int providerNum = 100;
        List<Provider> providers = new ArrayList<>();
        for (int i = 0; i < providerNum; i++) {
            providers.add(createProvider(i + "", 0, System.currentTimeMillis(), 1));
        }

        //创建请求
        int requestNum = 10000;
        List<RpcRequest> requests = new ArrayList<>(requestNum);
        for (int i = 0; i < requestNum; i++) {
            String key = i + "abc";
            RpcRequest request = new RpcRequest();
            request.setClazz("cn.zcn.rpc.Example");
            request.setMethodName("method");
            request.setParameterTypes(new String[] { String.class.getName() });
            request.setParameters(new Object[] { key });

            requests.add(request);
        }

        // 第一次负载均衡
        Map<RpcRequest, Provider> keyToSelect = new HashMap<>();
        for (RpcRequest req : requests) {
            Map<Provider, Integer> counter = doSelectWithRequest(1, providers, "consistentHash", req);
            Provider selected = counter.keySet().iterator().next();

            keyToSelect.put(req, selected);
        }

        // 随机删除一个节点
        providers.remove(ThreadLocalRandom.current().nextInt(providerNum));

        // 第二次选中的节点与第一次选中的节点不一致的请求的个数
        int missHits = 0;

        // 第二次负载均衡
        for (RpcRequest req : requests) {
            Map<Provider, Integer> counter = doSelectWithRequest(1, providers, "consistentHash", req);
            Provider selected = counter.keySet().iterator().next();

            if (!keyToSelect.get(req).equals(selected)) {
                missHits++;
            }
        }

        // 命中率大于98.5%
        float hitRate = (float) (requestNum - missHits) / (float) requestNum;
        System.out.println(hitRate);
        assertThat(hitRate).isGreaterThan(0.985f);
    }

    @Test
    public void testSelectWhenAddProvider() {
        //创建节点
        int providerNum = 100;
        List<Provider> providers = new ArrayList<>();
        for (int i = 0; i < providerNum; i++) {
            providers.add(createProvider(i + "", 0, System.currentTimeMillis(), 1));
        }

        //创建请求
        int requestNum = 10000;
        List<RpcRequest> requests = new ArrayList<>(requestNum);
        for (int i = 0; i < requestNum; i++) {
            String key = i + "abc";
            RpcRequest request = new RpcRequest();
            request.setClazz("cn.zcn.rpc.Example");
            request.setMethodName("method");
            request.setParameterTypes(new String[] { String.class.getName() });
            request.setParameters(new Object[] { key });

            requests.add(request);
        }

        // 第一次负载均衡
        Map<RpcRequest, Provider> keyToSelect = new HashMap<>();
        for (RpcRequest req : requests) {
            Map<Provider, Integer> counter = doSelectWithRequest(1, providers, "consistentHash", req);
            Provider selected = counter.keySet().iterator().next();

            keyToSelect.put(req, selected);
        }

        // 增加一个节点
        providers.add(createProvider("127.1.2.3", 0, System.currentTimeMillis(), 1));

        // 第二次选中的节点与第一次选中的节点不一致的请求的个数
        int missHits = 0;

        // 第二次负载均衡
        for (RpcRequest req : requests) {
            Map<Provider, Integer> counter = doSelectWithRequest(1, providers, "consistentHash", req);
            Provider selected = counter.keySet().iterator().next();

            if (!keyToSelect.get(req).equals(selected)) {
                missHits++;
            }
        }

        // 命中率大于98.5%
        float hitRate = (float) (requestNum - missHits) / (float) requestNum;
        assertThat(hitRate).isGreaterThan(0.985f);
    }
}
