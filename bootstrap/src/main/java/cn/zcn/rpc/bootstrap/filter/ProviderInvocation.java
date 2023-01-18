package cn.zcn.rpc.bootstrap.filter;

import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;
import cn.zcn.rpc.remoting.InvocationContext;

import java.lang.reflect.Method;

/**
 * @author zicung
 */
public class ProviderInvocation extends Invocation {

	private Method method;
	private Object instance;
	private InvocationContext invocationContext;

	private final RpcResponse response = new RpcResponse();

	public ProviderInvocation(RpcRequest request) {
		super(request);
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object getInstance() {
		return instance;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}

	public RpcResponse getResponse() {
		return response;
	}

	public InvocationContext getInvocationContext() {
		return invocationContext;
	}

	public void setInvocationContext(InvocationContext invocationContext) {
		this.invocationContext = invocationContext;
	}
}