package com.ratelimit.algo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedWindow extends RateLimiter {

	private final Map<Long, AtomicInteger> counter = new ConcurrentHashMap<>();
	
	protected FixedWindow(int maxReqPerUnitTime) {
		super(maxReqPerUnitTime);
	}

	@Override
	boolean allow() {
		long windowKey = System.currentTimeMillis()/1000 * 1000;
		counter.putIfAbsent(windowKey, new AtomicInteger(0));
		return counter.get(windowKey).incrementAndGet()<=maxReqPerUnitTime;
	}

}
