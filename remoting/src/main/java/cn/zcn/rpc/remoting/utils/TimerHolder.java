package cn.zcn.rpc.remoting.utils;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务调度器
 * 
 * @author zicung
 */
public class TimerHolder {
	private static volatile TimerHolder instance;

	private final Timer timer;

	private TimerHolder() {
		this.timer = new HashedWheelTimer(new NamedThreadFactory("rpc-timer",
				true), 10, TimeUnit.MILLISECONDS);
	}

	/**
	 * 获取定时任务调度器
	 * 
	 * @return {@code Timer}, 定时任务调度器
	 */
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
