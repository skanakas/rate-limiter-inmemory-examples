package com.ratelimit.algo;


/**
 * @author Sridhar
 *
 */
public abstract class RateLimiter {

  protected final int maxReqPerUnitTime;

  protected RateLimiter(int maxReqPerUnitTime) {
    this.maxReqPerUnitTime = maxReqPerUnitTime;
  }

  abstract boolean allow();
}
