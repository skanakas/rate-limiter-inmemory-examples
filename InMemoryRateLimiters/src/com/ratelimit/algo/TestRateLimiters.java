package com.ratelimit.algo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TestRateLimiters {

	public static void main(String[] args) throws InterruptedException {
		final int MAX_REQUESTS_PER_SEC = 10;

		//RateLimiter rateLimiter = TokenBucketFactory.getInstance(MAX_REQUESTS_PER_SEC);
		//RateLimiter rateLimiter = new TokenBucketLazyRefill(MAX_REQUESTS_PER_SEC);
		//RateLimiter rateLimiter = new SlidingWindowLog(MAX_REQUESTS_PER_SEC);
		RateLimiter rateLimiter = new SlidingWindow(MAX_REQUESTS_PER_SEC);

		Thread requestThread = new Thread(() -> {
			//sendRequest(rateLimiter, 20, 2);
			//sendRequest(rateLimiter,50, 5);
			//sendRequest(rateLimiter, 100, 5);
			sendRequest(rateLimiter,100, 12);
			//sendRequest(rateLimiter,200, 20);
			//sendRequest(rateLimiter,250, 25);
			//sendRequest(rateLimiter,500, 50);
			//sendRequest(rateLimiter,1000, 100);
		});

		requestThread.start();
		requestThread.join();
	}

	private static void sendRequest(RateLimiter rateLimiter, int totalCnt, int requestPerSec) {
		Map<Integer, Boolean> reqToStatusMap = new HashMap<>();
		long startTime = System.currentTimeMillis();
		CountDownLatch doneSignal = new CountDownLatch(totalCnt);
		for (int i = 0; i < totalCnt; i++) {
			try {
				final int reqID = i+1;
				
				new Thread(() -> {
					boolean allow = rateLimiter.allow();
					reqToStatusMap.put(reqID, allow);
					try {
						TimeUnit.MILLISECONDS.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					doneSignal.countDown();
				}).start();
				
				TimeUnit.MILLISECONDS.sleep(1000 / requestPerSec);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			doneSignal.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		double duration = (System.currentTimeMillis() - startTime) / 1000.0;
		System.out.println(totalCnt + " requests processed in " + duration + " seconds. "
				+ "Rate: " + (double) totalCnt / duration + " per second");

		Map<Boolean, Set<Integer>> status = new HashMap<>();
		reqToStatusMap.forEach((k,v)->{
			if(!status.containsKey(v))
				status.put(v, new HashSet<>());
			status.get(v).add(k);
		});
		System.out.println("**FINAL REPORT**");
		System.out.println("|Allowed\t|"+status.getOrDefault(true, new HashSet<>()).size());
		System.out.println("|NOT Allowed\t|"+status.getOrDefault(false, new HashSet<>()).size());
		System.out.println("**END REPORT**");
	}


}
