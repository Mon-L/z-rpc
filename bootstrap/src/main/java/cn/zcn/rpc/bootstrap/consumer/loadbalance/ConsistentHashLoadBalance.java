package cn.zcn.rpc.bootstrap.consumer.loadbalance;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.extension.Extension;
import cn.zcn.rpc.bootstrap.registry.Provider;

/**
 * 一致性哈希算法。
 *
 * @author zicung
 */
@Extension("consistentHash")
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    /** 管理每个接口方法的 {@code ConsistentHashSelector} */
    private final ConcurrentMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected Provider doSelect(List<Provider> providers, RpcRequest request) {
        String id = request.getIdentifier(request);
        ConsistentHashSelector selector = selectors.get(id);
        int h = providers.hashCode();
        if (selector == null || h != selector.hashcode) {
            selectors.put(id, new ConsistentHashSelector(h, providers));
            selector = selectors.get(id);
        }

        return selector.select(request);
    }

    private final static class ConsistentHashSelector {
        private final int hashcode;
        private final TreeMap<Long, Provider> providerMap = new TreeMap<>();

        private ConsistentHashSelector(int hashcode, List<Provider> providers) {
            this.hashcode = hashcode;

            MessageDigest md = getMessageDigest();
            for (Provider provider : providers) {
                for (int i = 0; i < 128; i++) {
                    String key = provider.getAddress() + i;
                    providerMap.put(hash(md, key), provider);
                }
            }
        }

        private Provider select(RpcRequest request) {
            StringBuilder key = new StringBuilder();
            for (Object obj : request.getParameters()) {
                key.append(obj);
            }

            long hash = hash(getMessageDigest(), key.toString());

            Map.Entry<Long, Provider> entry = providerMap.ceilingEntry(hash);
            if (entry != null) {
                return entry.getValue();
            }

            return providerMap.firstEntry().getValue();
        }

        private MessageDigest getMessageDigest() {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("md5");
            } catch (NoSuchAlgorithmException e) {
                throw new RpcException(e, e.getMessage());
            }

            return md;
        }

        private long hash(MessageDigest md, String key) {
            // Ketama hashing algorithm
            md.update(key.getBytes());
            byte[] digest = md.digest();

            return (((long) (digest[3] & 0xFF) << 24) |
                ((digest[2] & 0xFF) << 16) |
                ((digest[1] & 0xFF) << 8) |
                (digest[0] & 0xFF));
        }
    }
}
