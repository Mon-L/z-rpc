package cn.zcn.rpc.remoting.lifecycle;

import cn.zcn.rpc.remoting.exception.LifecycleException;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractLifecycle implements Lifecycle {

    private final AtomicBoolean started = new AtomicBoolean(false);

    @Override
    public void start() throws LifecycleException {
        if (started.compareAndSet(false, true)) {
            try {
                doStart();
            } catch (Throwable e) {
                if (e instanceof LifecycleException) {
                    throw e;
                } else {
                    throw new LifecycleException("Failed to start {0}. Error Msg: {1}", toString(), e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void stop() throws LifecycleException {
        if (started.compareAndSet(true, false)) {
            doStop();
        }
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

    protected abstract void doStart() throws LifecycleException;

    protected abstract void doStop() throws LifecycleException;
}
