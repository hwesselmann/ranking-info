package de.hdawg.rankinginfo.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.hdawg.rankinginfo.config.ApiProperties;

class RequestRateLimiterTest {

  private static RequestRateLimiter rateLimiter() {
    return new RequestRateLimiter(new ApiProperties(List.of()));
  }

  private static RequestRateLimiter rateLimiter(int maxBuckets) {
    return new RequestRateLimiter(
        new ApiProperties(List.of(), new ApiProperties.RateLimit(maxBuckets)));
  }

  @Test
  @DisplayName("allows requests within the limit")
  void allowsRequestsWithinLimit() {
    var rateLimiter = rateLimiter();

    assertTrue(rateLimiter.tryConsume("key-a"));
  }

  @Test
  @DisplayName("rejects requests once the bucket for a key is exhausted")
  void rejectsRequestsOnceBucketExhausted() {
    var rateLimiter = rateLimiter();

    for (int i = 0; i < 1000; i++) {
      assertTrue(rateLimiter.tryConsume("key-b"), "expected request " + i + " to be allowed");
    }

    assertFalse(rateLimiter.tryConsume("key-b"));
  }

  @Test
  @DisplayName("tracks separate buckets per key")
  void tracksSeparateBucketsPerKey() {
    var rateLimiter = rateLimiter();

    for (int i = 0; i < 1000; i++) {
      rateLimiter.tryConsume("exhausted-key");
    }
    assertFalse(rateLimiter.tryConsume("exhausted-key"));

    assertTrue(rateLimiter.tryConsume("fresh-key"));
  }

  @Test
  @DisplayName("never tracks more buckets than the configured maximum")
  void neverTracksMoreBucketsThanConfiguredMaximum() {
    var rateLimiter = rateLimiter(10);

    for (int i = 0; i < 5000; i++) {
      rateLimiter.tryConsume("rotating-key-" + i);
    }

    assertThat(rateLimiter.trackedBuckets()).isLessThanOrEqualTo(10);
  }

  @Test
  @DisplayName("falls back to the default bucket limit when none is configured")
  void fallsBackToDefaultBucketLimit() {
    var rateLimiter = rateLimiter();

    for (int i = 0; i < 12_000; i++) {
      rateLimiter.tryConsume("rotating-key-" + i);
    }

    assertThat(rateLimiter.trackedBuckets())
        .isLessThanOrEqualTo(ApiProperties.RateLimit.DEFAULT_MAX_BUCKETS);
  }

  @Test
  @DisplayName("keys that share a long prefix are still limited independently")
  void longKeysSharingPrefixAreLimitedIndependently() {
    var rateLimiter = rateLimiter();
    var prefix = "Bearer " + "x".repeat(8000);

    for (int i = 0; i < 1000; i++) {
      rateLimiter.tryConsume(prefix + "a");
    }

    assertFalse(rateLimiter.tryConsume(prefix + "a"));
    assertTrue(rateLimiter.tryConsume(prefix + "b"));
  }
}
