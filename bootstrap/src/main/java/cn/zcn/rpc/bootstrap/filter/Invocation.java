package cn.zcn.rpc.bootstrap.filter;

import cn.zcn.rpc.bootstrap.RpcRequest;

/**
 * @author zicung
 */
public abstract class Invocation {

	private final RpcRequest request;

	public Invocation(RpcRequest request) {
		this.request = request;
	}

	public RpcRequest getRequest() {
		return request;
	}
}