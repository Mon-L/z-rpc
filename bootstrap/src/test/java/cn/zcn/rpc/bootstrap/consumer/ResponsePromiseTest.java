package cn.zcn.rpc.bootstrap.consumer;

import cn.zcn.rpc.bootstrap.RpcResponse;
import org.assertj.core.data.Offset;
import org.junit.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.*;

/**
 * @author zicung
 */
public class ResponsePromiseTest {

    @Test
    public void testComplete() {
        ResponsePromise promise = new ResponsePromise();
        assertThat(promise.isDone()).isFalse();

        RpcResponse response = new RpcResponse();
        promise.complete(response);
        assertThat(promise.isDone()).isTrue();

        try {
            assertThat(promise.get()).isSameAs(response);
        } catch (Throwable t) {
            fail("Should not reach here.", t);
        }
    }

    @Test
    public void testCompleteExceptionally() {
        ResponsePromise promise = new ResponsePromise();
        assertThat(promise.isDone()).isFalse();

        Throwable cause = new IllegalArgumentException();
        promise.completeExceptionally(cause);
        assertThat(promise.isDone()).isTrue();

        assertThatExceptionOfType(ExecutionException.class).isThrownBy(promise::get)
            .withCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testCancel() {
        ResponsePromise promise = new ResponsePromise();
        assertThat(promise.isDone()).isFalse();

        promise.cancel(true);
        assertThat(promise.isDone()).isTrue();
        assertThat(promise.isCancelled()).isTrue();
        assertThatExceptionOfType(CancellationException.class).isThrownBy(promise::get);
    }

    @Test
    public void testGetThenSuccess() {
        ResponsePromise promise = new ResponsePromise();
        assertThat(promise.isDone()).isFalse();

        long waitTime = 500;
        RpcResponse response = new RpcResponse();
        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(waitTime);
                promise.complete(response);
            } catch (InterruptedException e) {
                fail("Should not reach here.", e);
            }
        }).start();

        try {
            long startTime = System.currentTimeMillis();
            promise.get();
            assertThat(promise.isDone()).isTrue();
            assertThat(System.currentTimeMillis() - startTime).isCloseTo(waitTime, Offset.offset(10L));
            assertThat(promise.get()).isSameAs(response);
        } catch (Throwable e) {
            fail("Should not reach here.", e);
        }
    }

    @Test
    public void testGetThenException() {
        ResponsePromise promise = new ResponsePromise();
        assertThat(promise.isDone()).isFalse();

        long waitTime = 400;
        Throwable cause = new IllegalArgumentException();
        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(waitTime);
                promise.completeExceptionally(cause);
            } catch (InterruptedException e) {
                fail("Should not reach here.", e);
            }
        }).start();

        long startTime = System.currentTimeMillis();
        assertThatExceptionOfType(ExecutionException.class).isThrownBy(() -> {
            try {
                promise.get();
            } catch (Throwable t) {
                assertThat(promise.isDone()).isTrue();
                assertThat(System.currentTimeMillis() - startTime).isCloseTo(waitTime, Offset.offset(10L));
                throw t;
            }
        }).withCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetThenTimeout() {
        ResponsePromise promise = new ResponsePromise();
        assertThat(promise.isDone()).isFalse();

        assertThatExceptionOfType(TimeoutException.class).isThrownBy(() -> {
            long waitTime = 300;
            long startTime = System.currentTimeMillis();
            try {
                promise.get(300, TimeUnit.MILLISECONDS);
            } catch (Throwable t) {
                assertThat(promise.isDone()).isFalse();
                assertThat(System.currentTimeMillis() - startTime).isCloseTo(waitTime, Offset.offset(10L));
                throw t;
            }
        });
    }
}
