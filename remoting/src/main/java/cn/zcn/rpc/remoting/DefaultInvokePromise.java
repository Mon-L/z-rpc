package cn.zcn.rpc.remoting;

import cn.zcn.rpc.remoting.protocol.ResponseCommand;
import io.netty.util.Timeout;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DefaultInvokePromise implements InvokePromise<ResponseCommand> {

    private final Promise<ResponseCommand> promise;
    private Timeout timeout;

    public DefaultInvokePromise(Promise<ResponseCommand> promise) {
        this.promise = promise;
    }

    @Override
    public void cancelTimeout() {
        if (timeout != null) {
            timeout.cancel();
        }
    }

    @Override
    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    @Override
    public InvokePromise<ResponseCommand> setSuccess(ResponseCommand result) {
        promise.setSuccess(result);
        return this;
    }

    @Override
    public boolean trySuccess(ResponseCommand result) {
        return promise.trySuccess(result);
    }

    @Override
    public InvokePromise<ResponseCommand> setFailure(Throwable cause) {
        promise.setFailure(cause);
        return this;
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        return promise.tryFailure(cause);
    }

    @Override
    public boolean setUncancellable() {
        return promise.setUncancellable();
    }

    @Override
    public InvokePromise<ResponseCommand> addListener(GenericFutureListener<? extends Future<? super ResponseCommand>> listener) {
        promise.addListener(listener);
        return this;
    }

    @Override
    public InvokePromise<ResponseCommand> addListeners(GenericFutureListener<? extends Future<? super ResponseCommand>>... listeners) {
        promise.addListeners(listeners);
        return this;
    }

    @Override
    public InvokePromise<ResponseCommand> removeListener(GenericFutureListener<? extends Future<? super ResponseCommand>> listener) {
        promise.removeListener(listener);
        return this;
    }

    @Override
    public InvokePromise<ResponseCommand> removeListeners(GenericFutureListener<? extends Future<? super ResponseCommand>>... listeners) {
        promise.removeListeners(listeners);
        return this;
    }

    @Override
    public InvokePromise<ResponseCommand> await() throws InterruptedException {
        promise.await();
        return this;
    }

    @Override
    public InvokePromise<ResponseCommand> awaitUninterruptibly() {
        promise.awaitUninterruptibly();
        return this;
    }

    @Override
    public InvokePromise<ResponseCommand> sync() throws InterruptedException {
        promise.sync();
        return this;
    }

    @Override
    public InvokePromise<ResponseCommand> syncUninterruptibly() {
        promise.syncUninterruptibly();
        return this;
    }

    @Override
    public boolean isSuccess() {
        return promise.isSuccess();
    }

    @Override
    public boolean isCancellable() {
        return promise.isCancellable();
    }

    @Override
    public Throwable cause() {
        return promise.cause();
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return promise.await(timeout, unit);
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return promise.await(timeoutMillis);
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        return promise.awaitUninterruptibly(timeout, unit);
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return promise.awaitUninterruptibly(timeoutMillis);
    }

    @Override
    public ResponseCommand getNow() {
        return promise.getNow();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return promise.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return promise.isCancelled();
    }

    @Override
    public boolean isDone() {
        return promise.isDone();
    }

    @Override
    public ResponseCommand get() throws InterruptedException, ExecutionException {
        return promise.get();
    }

    @Override
    public ResponseCommand get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return promise.get(timeout, unit);
    }
}
