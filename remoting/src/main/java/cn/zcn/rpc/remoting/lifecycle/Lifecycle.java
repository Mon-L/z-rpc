package cn.zcn.rpc.remoting.lifecycle;

import cn.zcn.rpc.remoting.exception.LifecycleException;

/**
 * 维护重要功能模块的生命周期，提供启动、关闭功能。
 *
 * @author zicung
 */
public interface Lifecycle {
    /**
     * 启动
     *
     * @throws LifecycleException 启动异常
     */
    void start() throws LifecycleException;

    /**
     * 停止
     *
     * @throws LifecycleException 停止异常
     */
    void stop() throws LifecycleException;

    /**
     * 是否已启动
     *
     * @return {@code true}，已启动；{@code false}，未启动
     */
    boolean isStarted();
}
