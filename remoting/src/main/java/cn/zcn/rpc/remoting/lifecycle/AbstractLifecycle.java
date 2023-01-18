package cn.zcn.rpc.remoting.lifecycle;

import cn.zcn.rpc.remoting.exception.LifecycleException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 抽象的 {@code Lifecycle} 实现。
 *
 * <p>使用 {@code started} 标识启动状态，当 {@code started.get() == false} 时才能调用 {@code start()}。 只有当 {@code
 * started.get() == true} 时才能调用 {@code stop()}。
 *
 * @author zicung
 */
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
                    throw new LifecycleException("Failed to start {}. Error Msg: {}", toString(), e.getMessage(), e);
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

    /**
     * 子类应该重写该方法执行启动逻辑
     *
     * @throws LifecycleException 启动异常
     */
    protected abstract void doStart() throws LifecycleException;

    /**
     * 子类应该重写该方法执行停止逻辑
     *
     * @throws LifecycleException 停止异常
     */
    protected abstract void doStop() throws LifecycleException;

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
