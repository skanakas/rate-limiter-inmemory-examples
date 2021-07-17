package com.ratelimit.algo;

import java.util.Comparator;
import java.util.PriorityQueue;

public class SlidingWindowLog extends RateLimiter {
	
	private PriorityQueue<Long> log = new PriorityQueue<>(new Comparator<Long>() {

		@Override
		public int compare(Long l1, Long l2) {
			return (int) (l1-l2);
		}
	});
	
	//private Queue<Long> log = new LinkedList<>();

	protected SlidingWindowLog(int maxReqPerUnitTime) {
		super(maxReqPerUnitTime);
	}

	/**
	 * 1. Remove old ones before the lower boundary
	 * 2. Add / log the new time
	 * 3. get the boundary size
	 * 4. check if size is less than the max allowed
	 * 5. Allow if inside the upper bound
	 */

	@Override
	boolean allow() {
		long curTime = System.currentTimeMillis()/1000 * 1000;
		long boundary = curTime - 1000;
		synchronized (log) {
			//1. Remove old ones before the lower boundary
			while (!log.isEmpty() && log.element() <= boundary) {
				log.poll();
			}
			//2. Add / log the new time
			log.add(curTime);
			boolean allow = log.size() <= maxReqPerUnitTime;
			System.out.println(curTime + ", log size = " + log.size()+", allow="+allow);
			return allow;
		}
	}

}
