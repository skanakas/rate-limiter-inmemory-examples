/**
 * 
 */
package com.ratelimit.algo;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author arun
 *
 */
public class TokenBucketFactory{

	private static class TokenBucket extends RateLimiter {
		private int tokens;
		private Lock lock = new ReentrantLock();

		private TokenBucket(int maxReqPerUnitTime) {
			super(maxReqPerUnitTime);
			this.tokens = maxReqPerUnitTime;
		}

		@Override
		public boolean allow() {
			try {
				lock.lock();

				if (tokens == 0) {
					return false;
				}
				tokens--;
				return true;
			} finally {
				lock.unlock();
			}

		}

		void init() {
			Thread dt = new Thread(()->{
				daemonThread();
			});
			dt.setDaemon(true);
			dt.start();
		}

		private void daemonThread() {
			while(true) {
				lock.lock();
				tokens = Math.min(tokens + maxReqPerUnitTime, maxReqPerUnitTime);
				lock.unlock();
				
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private TokenBucketFactory() {}

	public static RateLimiter getInstance(int cap) {
		TokenBucket rt = new TokenBucket(cap);
		rt.init();
		return rt;
	}

}
