package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.RpcStatus;

/**
 * 请求调用上下文
 * 
 * @author zicung
 */
public interface InvocationContext {
	/**
	 * 请求方
	 * 
	 * @return remote ip
	 */
	String getRemoteHost();

	/**
	 * 请求方端口
	 * 
	 * @return remote port
	 */
	int getRemotePort();

	/**
	 * 请求ID
	 * 
	 * @return request id
	 */
	int getRequestId();

	/**
	 * 请求进入等待队列的时间
	 * 
	 * @return 请求准备执行的时间
	 */
	long getReadyTimeMillis();

	/**
	 * 请求开始执行的时间，指请求分配到了线程开始执行的时间。
	 * 
	 * @return 请求开始执行的时间
	 */
	long getStartTimeMillis();

	/**
	 * 请求处理是否超时
	 * 
	 * @return {@code true}，请求已超时；{@code false}，请求未超时
	 */
	boolean isTimeout();

	/**
	 * 获取剩余的超时时间
	 * 
	 * @return 距离超时还剩多少毫秒
	 */
	int getRemainingTime();

	/**
	 * 返回成功响应
	 * 
	 * @param obj
	 *            响应信息
	 */
	void writeAndFlushResponse(Object obj);

	/**
	 * 返回响应
	 * 
	 * @param obj
	 *            响应信息
	 * @param status
	 *            响应状态码
	 */
	void writeAndFlushResponse(Object obj, RpcStatus status);
}
