package de.hdawg.rankinginfo.api.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RequestRateLimiterTest {

  @Test
  @DisplayName("allows requests within the limit")
  void allowsRequestsWithinLimit() {
    var rateLimiter = new RequestRateLimiter();

    assertTrue(rateLimiter.tryConsume("key-a"));
  }

  @Test
  @DisplayName("rejects requests once the bucket for a key is exhausted")
  void rejectsRequestsOnceBucketExhausted() {
    var rateLimiter = new RequestRateLimiter();

    for (int i = 0; i < 1000; i++) {
      assertTrue(rateLimiter.tryConsume("key-b"), "expected request " + i + " to be allowed");
    }

    assertFalse(rateLimiter.tryConsume("key-b"));
  }

  @Test
  @DisplayName("tracks separate buckets per key")
  void tracksSeparateBucketsPerKey() {
    var rateLimiter = new RequestRateLimiter();

    for (int i = 0; i < 1000; i++) {
      rateLimiter.tryConsume("exhausted-key");
    }
    assertFalse(rateLimiter.tryConsume("exhausted-key"));

    assertTrue(rateLimiter.tryConsume("fresh-key"));
  }
}
