package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.extension.Extension;
import cn.zcn.rpc.bootstrap.registry.Provider;
import cn.zcn.rpc.bootstrap.utils.Md5Util;

/**
 * 使用 Ketama hashing algorithm 且带权重的一致性哈希算法。
 *
 * @author zicung
 */
@Extension("consistentHash")
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    /** 管理每个接口方法的 {@code WeightConsistentHashSelector} */
    private final ConcurrentMap<String, WeightedConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected Provider doSelect(List<Provider> providers, RpcRequest request) {
        String id = request.getIdentifier(request);
        WeightedConsistentHashSelector selector = selectors.get(id);
        int hashcode = providers.hashCode();
        if (selector == null || hashcode != selector.hashcode) {
            selectors.put(id, new WeightedConsistentHashSelector(hashcode, providers));
            selector = selectors.get(id);
        }

        return selector.select(request);
    }

    private final static class WeightedConsistentHashSelector {

        private static final int DEFAULT_REPLICA_NUM = 160;

        private final int hashcode;
        private final TreeMap<Long, Provider> nodes = new TreeMap<>();

        private WeightedConsistentHashSelector(int hashcode, List<Provider> providers) {
            this.hashcode = hashcode;

            boolean isSameWeight = true;
            int totalWeight = 0, firstWeight = providers.get(0).getWeight();
            for (Provider provider : providers) {
                totalWeight += provider.getWeight();

                if (isSameWeight && firstWeight != totalWeight) {
                    isSameWeight = false;
                }
            }

            if (isSameWeight) {
                for (Provider provider : providers) {
                    for (int i = 0; i < DEFAULT_REPLICA_NUM / 4; i++) {
                        for (long position : getKetamaNodePositions(provider.getAddress() + "-" + i)) {
                            nodes.put(position, provider);
                        }
                    }
                }
            } else {
                for (Provider provider : providers) {
                    float percent = (float) provider.getWeight() / (float) totalWeight;
                    int replicaNum = (int) (Math.floor(
                        percent * (float) providers.size() * (float) DEFAULT_REPLICA_NUM / 4) * 4);
                    for (int i = 0; i < replicaNum / 4; i++) {
                        for (long position : getKetamaNodePositions(provider.getAddress() + "-" + i)) {
                            nodes.put(position, provider);
                        }
                    }
                }
            }
        }

        private Provider select(RpcRequest request) {
            StringJoiner keyJoiner = new StringJoiner(",");
            for (Object obj : request.getParameters()) {
                keyJoiner.add(obj.toString());
            }

            byte[] digest = Md5Util.computeMd5(keyJoiner.toString());
            long hash = getKetamaHash(digest, 0);

            Map.Entry<Long, Provider> entry = nodes.ceilingEntry(hash);
            if (entry == null) {
                entry = nodes.firstEntry();
            }

            return entry.getValue();
        }

        private List<Long> getKetamaNodePositions(String key) {
            List<Long> positions = new ArrayList<>();
            byte[] digest = Md5Util.computeMd5(key);

            for (int h = 0; h < 4; h++) {
                positions.add(getKetamaHash(digest, h));
            }

            return positions;
        }

        private long getKetamaHash(byte[] bytes, int i) {
            //@formatter:off
            long hash = ((long) (bytes[3 + i * 4] & 0xFF) << 24)
                    | ((long) (bytes[2 + i * 4] & 0xFF) << 16)
                    | ((long) (bytes[1 + i * 4] & 0xFF) << 8)
                    | (bytes[0] & 0xFF);
            //@formatter:on

            /* Truncate to 32-bits */
            return hash & 0xffffffffL;
        }
    }
}
