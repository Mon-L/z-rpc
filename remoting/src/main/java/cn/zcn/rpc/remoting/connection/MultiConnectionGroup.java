package cn.zcn.rpc.remoting.connection;

import cn.zcn.rpc.remoting.Url;
import cn.zcn.rpc.remoting.utils.NetUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 管理包含多条 {@link Connection} 的连接组。{@code Connection} 不能被多条 {@code Thread} 同时使用。
 * 只有当 {@code Connection} 被释放后才可以被其他 {@code Thread} 使用。
 *
 * @author zicung
 */
public class MultiConnectionGroup extends AbstractConnectionGroup {

    private final int maxConnection;
    private final long acquireTimeoutMillis;

    private final AtomicInteger lendingCount = new AtomicInteger(0);
    private final Deque<Connection> connections = new LinkedList<>();
    private final Deque<PendingAcquireTask> pendingAcquireTasks = new LinkedList<>();

    private volatile Promise<Void> closePromise;

    public MultiConnectionGroup(Url url, Bootstrap bootstrap, int maxConnectionNum, long acquireTimeoutMillis) {
        super(url, bootstrap);
        this.maxConnection = maxConnectionNum;
        this.acquireTimeoutMillis = acquireTimeoutMillis;
    }

    /**
     * 获取可用连接。<p>
     * 获取连接时会出现以下情况：
     * <ul>
     *     <li>当 {@code lendingCount < maxConnection} 时会新建一条 {@code Connection} 并返回</li>
     *     <li>当 {@code lendingCount >= maxConnection}，等待其他线程释放 {@code Connection} 后才能获得 {@code Connection}。</li>
     *     <li>当获得一条 {@code Connection} 时会检查其是否存活，当 {@code Connection} 不存活是会移除它并重新获取。</li>
     * </ul>
     *
     * @param promise acquired promise
     */
    @Override
    protected void doAcquireConnection(Promise<Connection> promise) {
        if (lendingCount.get() < maxConnection) {
            lendingCount.incrementAndGet();

            Promise<Connection> newPromise = executor.newPromise();
            AcquiredListener listener = new AcquiredListener(promise);
            newPromise.addListener(listener);

            getOrCreateConnection(newPromise);
        } else {
            PendingAcquireTask task = new PendingAcquireTask(promise);
            task.timeout = executor.schedule(task::onTimeout, acquireTimeoutMillis, TimeUnit.MILLISECONDS);
            pendingAcquireTasks.offerLast(task);
        }
    }

    /**
     * 获取或者新建 {@code Connection}。当重用 {@code Connection} 时检查其是否存活
     *
     * @param promise acquired promise
     */
    private void getOrCreateConnection(Promise<Connection> promise) {
        Connection conn = connections.pollFirst();
        if (conn == null) {
            createConnection(promise);
        } else {
            if (conn.isActive()) {
                promise.setSuccess(conn);
            } else {
                getOrCreateConnection(promise);
            }
        }
    }

    private void runPendingAcquireTasks() {
        while (lendingCount.get() < maxConnection && !pendingAcquireTasks.isEmpty()) {
            PendingAcquireTask task = pendingAcquireTasks.pollFirst();
            task.timeout.cancel(false);
            task.execute();
        }
    }

    @Override
    protected void doReleaseConnection(Promise<Void> promise, Connection connection) {
        Url groupKey = connection.getChannel().attr(Connection.CONNECTION_GROUP_KEY).get();
        if (!this.url.equals(groupKey)) {
            connection.close();
            promise.setFailure(new IllegalArgumentException("Connection " + NetUtil.getRemoteAddress(connection.getChannel()) +
                    " was not acquired from this ConnectionGroup"));
        } else {
            lendingCount.decrementAndGet();

            if (!connection.isActive()) {
                promise.setSuccess(null);
            } else {
                connections.offerLast(connection);
                promise.setSuccess(null);
            }

            runPendingAcquireTasks();
        }
    }

    @Override
    public int getActiveCount() {
        int count = lendingCount.get();
        for (Connection conn : connections) {
            count += conn.isActive() ? 1 : 0;
        }

        return count;
    }

    @Override
    public boolean canClose() {
        if (lendingCount.get() > 0) {
            return false;
        }

        if (connections.isEmpty()) {
            return true;
        }

        for (Connection conn : connections) {
            if (conn.isActive() && !conn.getInvokeFutures().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Future<Void> close() {
        if (isClosed.compareAndSet(false, true)) {
            this.closePromise = executor.newPromise();

            if (!connections.isEmpty()) {
                AtomicInteger progress = new AtomicInteger(connections.size());
                while (!connections.isEmpty()) {
                    connections.pollFirst().close().addListener(cf -> {
                        if (cf.isSuccess()) {
                            if (progress.decrementAndGet() == 0) {
                                this.closePromise.setSuccess(null);
                            }
                        } else {
                            this.closePromise.setFailure(cf.cause());
                        }
                    });
                }
            } else {
                this.closePromise.setSuccess(null);
            }
        }
        return closePromise;
    }

    private class PendingAcquireTask {
        private final Promise<Connection> newPromise;
        public ScheduledFuture<?> timeout;
        private volatile boolean executed = false;

        public PendingAcquireTask(Promise<Connection> ordinalPromise) {
            this.newPromise = executor.newPromise();
            this.newPromise.addListener(new AcquiredListener(ordinalPromise));
        }

        private void execute() {
            if (!executed) {
                executed = true;
                doAcquireConnection(newPromise);
            }
        }

        private void onTimeout() {
            if (executed) {
                lendingCount.decrementAndGet();
            } else {
                executed = true;
            }

            newPromise.setFailure(new TimeoutException("Acquire connection timeout."));
        }
    }

    private class AcquiredListener implements GenericFutureListener<Future<Connection>> {

        private final Promise<Connection> promise;

        public AcquiredListener(Promise<Connection> promise) {
            this.promise = promise;
        }

        @Override
        public void operationComplete(Future<Connection> future) throws Exception {
            if (isClosed.get()) {
                lendingCount.decrementAndGet();
                if (future.isSuccess()) {
                    future.get().close();
                }
                reportGroupClosed(promise);
            } else {
                if (future.isSuccess()) {
                    promise.setSuccess(future.get());
                } else {
                    lendingCount.decrementAndGet();
                    runPendingAcquireTasks();
                    promise.setFailure(future.cause());
                }
            }
        }
    }
}
