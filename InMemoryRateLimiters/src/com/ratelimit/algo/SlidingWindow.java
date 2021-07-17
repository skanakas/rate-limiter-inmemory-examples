package com.ratelimit.algo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SlidingWindow extends RateLimiter {

	// TODO: Clean up stale entries
	private final ConcurrentMap<Long, AtomicInteger> windows = new ConcurrentHashMap<>();

	protected SlidingWindow(int maxRequestPerSec) {
		super(maxRequestPerSec);
	}

	/**
	 * Previous window Weight = % (CurrentRollingWindowRequests in PreviousFixedWindow) = (1 - (currentTime - currentWindowKey) / 1000.0)
	 * 
	 * Current Window Count = PreviousFixedWindowCount * Previous window Weight + currentFixedWindowCount
	 */

	@Override
	boolean allow() {
		/**
		 * Add current req key details in the window data store
		 */
		long curTime = System.currentTimeMillis();
		long curWindowKey = curTime / 1000 * 1000;
		windows.putIfAbsent(curWindowKey, new AtomicInteger(0));

		/**
		 * Fetch Previous window details
		 */
		long preWindowKey = curWindowKey - 1000;
		AtomicInteger preCount = windows.get(preWindowKey);
		if (preCount == null) {
			return windows.get(curWindowKey).incrementAndGet() <= maxReqPerUnitTime;
		}

		/**
		 * Apply the formulae for sliding window
		 */
		double preWeight = 1 - (curTime - curWindowKey) / 1000.0;
		long count = (long) (preCount.get() * preWeight + windows.get(curWindowKey).get());
		boolean allow = count <= maxReqPerUnitTime;
		
		System.out.println("count="+count+" | "+preCount.get()+"(prevCount) * "+preWeight+"(preWeight) + "+windows.get(curWindowKey).incrementAndGet()+"(curWindowKey)"+"  ALLOW="+allow);
		
		
		return allow;
	}
}
