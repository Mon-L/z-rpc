package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.registry.Provider;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public Provider select(List<Provider> providers, RpcRequest request) {
        if (providers.isEmpty()) {
            return null;
        } else if (providers.size() == 1) {
            return providers.get(0);
        }

        return doSelect(providers, request);
    }

    protected int adjustWeightWithWarmup(int weight, long startTime, int warmup) {
        if (weight <= 0) {
            return 0;
        }

        if (startTime <= 0 || warmup <= 0) {
            return weight;
        }

        //运行时间
        long uptime = System.currentTimeMillis() - startTime;
        if (uptime <= 0) {
            return 1;
        }

        //运行时间小于预热时间
        if (uptime < warmup) {
            return (int) (((float) uptime / warmup) * weight);
        }

        return weight;
    }

    protected abstract Provider doSelect(List<Provider> providers, RpcRequest request);
}
