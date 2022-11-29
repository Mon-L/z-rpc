package cn.zcn.rpc.remoting.lifecycle;

import cn.zcn.rpc.remoting.exception.LifecycleException;

public interface Lifecycle {
    void start() throws LifecycleException;

    void stop() throws LifecycleException;

    boolean isStarted();
}
