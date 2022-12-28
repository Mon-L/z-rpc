package cn.zcn.rpc.remoting.utils;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.TimeUnit;

public class TimerHolder {

    private static volatile TimerHolder instance;

    private final Timer timer;

    private TimerHolder() {
        this.timer = new HashedWheelTimer(new NamedThreadFactory("rpc-timer"), 10,
                TimeUnit.MILLISECONDS);
    }

    public static Timer getTimer() {
        if (instance == null) {
            synchronized (TimerHolder.class) {
                if (instance == null) {
                    instance = new TimerHolder();
                }
            }
        }
        
        return instance.timer;
    }
}
