package de.hdawg.rankinginfo.api.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import de.hdawg.rankinginfo.config.ApiProperties;

/// Shared token-bucket rate limiter for the REST and gRPC APIs: 1000 requests per hour per key,
/// where the key is the caller's bearer token or, failing that, its remote address.
///
/// Both protocols rate-limit *before* authenticating, so unauthenticated callers can insert
/// entries here. The cache is therefore bounded on two axes:
/// - **Entry count** — `maximumSize` caps how many buckets exist, so rotating keys cannot grow the
///   cache without limit. Eviction means a caller whose bucket was evicted starts over with a full
///   allowance, which is the accepted trade-off for a fixed memory ceiling.
/// - **Entry size** — keys are hashed before use, so an oversized `Authorization` header cannot be
///   retained verbatim.
@Component
public class RequestRateLimiter {

  private static final Duration BUCKET_TTL = Duration.ofHours(2);
  private static final int CAPACITY_PER_HOUR = 1000;

  private final Cache<String, Bucket> buckets;

  public RequestRateLimiter(ApiProperties apiProperties) {
    this.buckets =
        Caffeine.newBuilder()
            .maximumSize(apiProperties.rateLimit().maxBuckets())
            .expireAfterAccess(BUCKET_TTL)
            .build();
  }

  /// Consumes a single token from the bucket belonging to `key`.
  ///
  /// @param key the caller identity: the raw `Authorization` header value, or the remote address
  ///     when no such header is present. Untrusted and unbounded in length; hashed internally
  /// @return `true` if the request is within the limit, `false` if the bucket is exhausted
  public boolean tryConsume(String key) {
    var bucket = buckets.get(hash(key), k -> createBucket());
    return bucket.tryConsume(1);
  }

  /// Number of buckets currently held, for monitoring how close the limiter runs to its ceiling.
  ///
  /// @return the tracked bucket count after pending evictions have been applied
  public long trackedBuckets() {
    buckets.cleanUp();
    return buckets.estimatedSize();
  }

  private static String hash(String key) {
    try {
      var digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(key.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private static Bucket createBucket() {
    return Bucket.builder()
        .addLimit(
            Bandwidth.builder()
                .capacity(CAPACITY_PER_HOUR)
                .refillGreedy(CAPACITY_PER_HOUR, Duration.ofHours(1))
                .build())
        .build();
  }
}
