package cn.zcn.rpc.bootstrap.filter;

import cn.zcn.rpc.bootstrap.InvokeType;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.consumer.ResponsePromise;
import cn.zcn.rpc.bootstrap.registry.Provider;

/**
 * @author zicung
 */
public class ConsumerInvocation extends Invocation {

    /** 路由后被选中的服务提供者 */
    private Provider provider;

    /** 调用类型 */
    private InvokeType invokeType = InvokeType.SYNC;

    /** 请求超时时间 */
    private int timeout;

    private final ResponsePromise responsePromise = new ResponsePromise();

    public ConsumerInvocation(RpcRequest request) {
        super(request);
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public ResponsePromise getResponsePromise() {
        return responsePromise;
    }

    public InvokeType getInvokeType() {
        return invokeType;
    }

    public void setInvokeType(InvokeType invokeType) {
        this.invokeType = invokeType;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}