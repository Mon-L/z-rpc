package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.RpcException;
import cn.zcn.rpc.bootstrap.RpcRequest;
import cn.zcn.rpc.bootstrap.RpcResponse;

/**
 * Rpc 调用者，提供调用远程接口的功能。
 * 
 * @author zicung
 */
public interface RpcInvoker {

	/**
	 * 调用远程接口
	 * 
	 * @param request
	 *            当前请求
	 * @return 响应
	 * @throws RpcException
	 *             调用异常
	 */
	RpcResponse invoke(RpcRequest request) throws RpcException;
}
