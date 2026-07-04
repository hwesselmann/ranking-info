package de.hdawg.rankinginfo.api.security;

import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

@Component
public class RequestRateLimiter {

  private final Cache<String, Bucket> buckets =
      Caffeine.newBuilder().expireAfterAccess(Duration.ofHours(2)).build();

  public boolean tryConsume(String key) {
    var bucket = buckets.get(key, k -> createBucket());
    return bucket.tryConsume(1);
  }

  private static Bucket createBucket() {
    return Bucket.builder()
        .addLimit(
            Bandwidth.builder().capacity(1000).refillGreedy(1000, Duration.ofHours(1)).build())
        .build();
  }
}
