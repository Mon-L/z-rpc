package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.extension.Extension;
import cn.zcn.rpc.bootstrap.registry.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 平滑加权轮询算法（smooth weighted round-robin）。
 *
 * @author zicung
 */
@Extension("roundRobin")
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    /**
     * {@code WeightedRoundRobinNode} 回收时间， 10min。
     *
     * <pre>
     * {@code System.currentTimeMillis() - WeightedRoundRobinNode.lastUpdate > RECYCLE_TIME} 时，
     * {@code WeightedRoundRobinNode} 将从 {@code methodWeights} 中移除。
     * </pre>
     */
    private static final int RECYCLE_TIME = 600000;

    private static final class WeightedRoundRobinNode {
        /** 节点权重，值为 {@code Provider} 的权重*/
        private int weight;

        /** 节点最后更新时间 */
        private long lastUpdate;

        /** 节点当前权重值 */
        private final AtomicLong curWeight;

        private WeightedRoundRobinNode(int weight) {
            this.weight = weight;
            this.curWeight = new AtomicLong(0);
        }

        private void setWeight(int weight) {
            this.weight = weight;
            curWeight.set(0);
        }

        private long increaseWeight() {
            return curWeight.addAndGet(weight);
        }

        private void decreaseWeight(long totalWeight) {
            curWeight.addAndGet(-1 * totalWeight);
        }
    }

    // （method -> (provider -> weightNode)）
    private final ConcurrentMap<String, ConcurrentMap<String, WeightedRoundRobinNode>> methodWeights = new ConcurrentHashMap<>();

    @Override
    protected Provider doSelect(List<Provider> providers, RpcRequest request) {
        String id = request.getIdentifier(request);
        ConcurrentMap<String, WeightedRoundRobinNode> providerWeights = methodWeights.computeIfAbsent(id,
            s -> new ConcurrentHashMap<>());

        long totalWeight = 0, maxWeight = Integer.MIN_VALUE, now = System.currentTimeMillis();
        Provider selected = null;
        WeightedRoundRobinNode selectedWrr = null;

        for (Provider provider : providers) {
            int weight = adjustWeightWithWarmup(provider.getWeight(), provider.getStartTime(), provider.getWarmup());

            WeightedRoundRobinNode wrr = providerWeights.computeIfAbsent(provider.getAddress(),
                s -> new WeightedRoundRobinNode(weight));

            if (provider.getWeight() != wrr.weight) {
                wrr.setWeight(weight);
            }

            wrr.lastUpdate = now;
            long curWeight = wrr.increaseWeight();

            if (curWeight > maxWeight) {
                maxWeight = curWeight;
                selected = provider;
                selectedWrr = wrr;
            }

            totalWeight += weight;
        }

        if (providers.size() != providerWeights.size()) {
            providerWeights.values().removeIf(node -> now - node.lastUpdate > RECYCLE_TIME);
        }

        if (selected != null) {
            selectedWrr.decreaseWeight(totalWeight);
            return selected;
        }

        return providers.get(0);
    }
}
