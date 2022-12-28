package cn.zcn.rpc.remoting.connection;

import cn.zcn.rpc.remoting.Url;
import cn.zcn.rpc.remoting.exception.LifecycleException;
import cn.zcn.rpc.remoting.lifecycle.AbstractLifecycle;
import cn.zcn.rpc.remoting.utils.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;

public class ConnectionGroupManager extends AbstractLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionGroupManager.class);

    private final Bootstrap bootstrap;
    private final ConcurrentMap<Url, ConnectionGroup> connectionGroups = new ConcurrentHashMap<>();
    private final Queue<ConnectionGroup> pendingCloseConnectionGroups = new ArrayDeque<>();

    private ScheduledExecutorService connectionGroupCleaner;

    public ConnectionGroupManager(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    protected void doStart() throws LifecycleException {
        this.connectionGroupCleaner = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("connection-group-cleaner"));
        this.connectionGroupCleaner.scheduleAtFixedRate(this::cleanConnectionGroup, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    protected void doStop() throws LifecycleException {
        connectionGroupCleaner.shutdown();

        Iterator<ConnectionGroup> iter = connectionGroups.values().iterator();
        while (iter.hasNext()) {
            pendingCloseConnectionGroups.add(iter.next());
            iter.remove();
        }

        //等待所有连接组被关闭
        long startTime = System.currentTimeMillis();
        while (!pendingCloseConnectionGroups.isEmpty()) {
            try {
                cleanConnectionGroup();
                TimeUnit.MILLISECONDS.sleep(100);
                LOGGER.warn("Wait for all connection groups be closed. Total wait time:{}ms", System.currentTimeMillis() - startTime);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void cleanConnectionGroup() {
        while (!pendingCloseConnectionGroups.isEmpty()) {
            ConnectionGroup group = pendingCloseConnectionGroups.poll();
            if (!group.canClose()) {
                pendingCloseConnectionGroups.add(group);
            } else {
                closeConnectionGroup(group);
            }
        }

        Iterator<ConnectionGroup> iter = connectionGroups.values().iterator();
        while (iter.hasNext()) {
            ConnectionGroup group = iter.next();

            //清理没有可用连接且长时间没被访问过的连接组
            if (group.getActiveCount() <= 0 && System.currentTimeMillis() - group.getLastAcquiredTime() > 300000) {
                iter.remove();
                pendingCloseConnectionGroups.add(group);

                LOGGER.info("Remove connection group. Url:{}", group.getURL().toString());
            }
        }
    }

    private void closeConnectionGroup(ConnectionGroup group) {
        group.close().addListener(future -> {
            if (future.isSuccess()) {
                LOGGER.info("Connection group was closed. Url:{}", group.getURL().toString());
            } else {
                LOGGER.warn("Failed to close connection group. Url:{} " + group.getURL().toString());
            }
        });
    }

    public ConnectionGroup getConnectionGroup(Url url) throws IllegalStateException {
        if (!isStarted()) {
            throw new IllegalStateException("ConnectionGroupManager was closed.");
        }

        ConnectionGroup group = connectionGroups.get(url);
        if (group == null) {
            int maxConnections = url.getMaxConnectionNum();

            if (maxConnections < 1) {
                throw new IllegalArgumentException("Max connection num must be equal or greater than 1.");
            }

            group = (maxConnections == 1 ? new SingleConnectionGroup(url, bootstrap.clone()) :
                    new MultiConnectionGroup(url, bootstrap.clone(), maxConnections, 10000));

            ConnectionGroup exist = connectionGroups.putIfAbsent(url, group);
            if (exist != null) {
                closeConnectionGroup(group);
                group = exist;
            }
        }
        return group;
    }
}
