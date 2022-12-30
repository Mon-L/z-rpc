package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.extension.Extension;
import cn.zcn.rpc.bootstrap.registry.Provider;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权随机算法。根据权重随机选择一个服务提供者
 */
@Extension("random")
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected Provider doSelect(List<Provider> providers, RpcRequest request) {
        int len = providers.size();
        int[] weights = new int[len];
        int totalWeight = 0;

        //是否所有节点权重都一样
        boolean sameWeight = true;

        for (int i = 0; i < len; i++) {
            Provider provider = providers.get(i);
            int weight = adjustWeightWithWarmup(provider.getWeight(), provider.getStartTime(), provider.getWarmup());
            totalWeight += weight;
            weights[i] = totalWeight;

            if (sameWeight && weight != weights[0]) {
                sameWeight = false;
            }
        }

        if (!sameWeight && totalWeight > 0) {
            long offset = ThreadLocalRandom.current().nextInt(totalWeight);
            for (int i = 0; i < len; i++) {
                if (offset < weights[i]) {
                    return providers.get(i);
                }
            }
        }

        return providers.get(ThreadLocalRandom.current().nextInt(len));
    }
}
