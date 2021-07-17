package com.ratelimit.algo;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucketLazyRefill extends RateLimiter {
	int tokens = 0;
	private Lock lock = new ReentrantLock();
	long lastRefillTimeInMs = System.currentTimeMillis();

	protected TokenBucketLazyRefill(int maxReqPerUnitTime) {
		super(maxReqPerUnitTime);
		this.tokens = maxReqPerUnitTime;
	}

	@Override
	boolean allow() {
		try {
			lock.lock();
			refillTokens();
			if (tokens == 0) {
				return false;
			}
			tokens--;
			return true;
		} finally {
			lock.unlock();
		}
	}

	private void refillTokens() {
		long currTime = System.currentTimeMillis();
		double diffInSec = ((currTime - lastRefillTimeInMs) / 1000);
		int refillCnt = (int) (diffInSec * maxReqPerUnitTime);
		if(refillCnt>0) {
			tokens = Math.min(tokens+refillCnt, maxReqPerUnitTime);
			lastRefillTimeInMs = currTime;
		}
	}

}
