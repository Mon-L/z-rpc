package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.registry.Provider;

import java.util.List;

/**
 * 负载均衡抽象父类
 *
 * @author zicung
 */
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

    /**
     * 当服务运行时间小于预热时间时降低其权重。调整后的服务权重处于 [0, weight] 之间。
     *
     * @param weight    原始权重
     * @param startTime 服务启动时间
     * @param warmup    服务预热时间
     * @return 调整后当权重
     */
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

    /**
     * 子类应覆盖此方法用于执行负载均衡的逻辑
     *
     * @param providers 服务提供者列表
     * @param request   请求
     * @return 被选中的服务提供者
     */
    protected abstract Provider doSelect(List<Provider> providers, RpcRequest request);
}
