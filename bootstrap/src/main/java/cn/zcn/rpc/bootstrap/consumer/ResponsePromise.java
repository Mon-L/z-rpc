package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.RpcResponse;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zicung
 */
public class ResponsePromise implements Future<RpcResponse> {

    private static final CancellationException CANCELLATION_CAUSE = new CancellationException();

    private final AtomicReference<Object> outcome = new AtomicReference<>();
    private final CountDownLatch latch = new CountDownLatch(1);

    public void complete(RpcResponse response) {
        if (isDone()) {
            throw new IllegalStateException("ResponsePromise is completed. ResponsePromise:" + this);
        }

        if (outcome.compareAndSet(null, response)) {
            latch.countDown();
        }
    }

    public void completeExceptionally(Throwable cause) {
        if (isDone()) {
            throw new IllegalStateException("ResponsePromise is completed. ResponsePromise:" + this, cause);
        }

        if (outcome.compareAndSet(null, cause)) {
            latch.countDown();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!isDone()) {
            if (outcome.compareAndSet(null, CANCELLATION_CAUSE)) {
                latch.countDown();
            }
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return outcome.get() == CANCELLATION_CAUSE;
    }

    @Override
    public boolean isDone() {
        return outcome.get() != null;
    }

    @Override
    public RpcResponse get() throws InterruptedException, ExecutionException {
        try {
            wait0(null, TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            throw new ExecutionException(e);
        }

        return report();
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        if (wait0(timeout, unit)) {
            report();
        }

        throw new TimeoutException();
    }

    private boolean wait0(Long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (isDone()) {
            return true;
        } else {
            if (timeout == null) {
                latch.await();
                return true;
            } else {
                long waitTime = unit.toNanos(timeout);
                if (latch.await(waitTime, TimeUnit.NANOSECONDS)) {
                    return true;
                }

                return isDone();
            }
        }
    }

    private RpcResponse report() throws ExecutionException {
        Object o = outcome.get();
        if (o instanceof CancellationException) {
            throw ((CancellationException) o);
        } else if (o instanceof Throwable) {
            throw new ExecutionException((Throwable) o);
        }

        return (RpcResponse) o;
    }
}
