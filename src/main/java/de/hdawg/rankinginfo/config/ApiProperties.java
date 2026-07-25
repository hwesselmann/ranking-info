package de.hdawg.rankinginfo.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

/// Configuration for the public API, bound from the `api.*` properties.
///
/// @param tokens accepted bearer tokens; `null` is treated as an empty list
/// @param rateLimit rate-limiter tuning; `null` falls back to [RateLimit#defaults()]
@ConfigurationProperties(prefix = "api")
public record ApiProperties(List<String> tokens, RateLimit rateLimit) {

  @ConstructorBinding
  public ApiProperties(List<String> tokens, RateLimit rateLimit) {
    this.tokens = tokens == null ? List.of() : List.copyOf(tokens);
    this.rateLimit = rateLimit == null ? RateLimit.defaults() : rateLimit;
  }

  /// Convenience constructor applying the default rate-limit settings.
  ///
  /// @param tokens accepted bearer tokens
  public ApiProperties(List<String> tokens) {
    this(tokens, null);
  }

  /// Tuning for the shared request rate limiter.
  ///
  /// @param maxBuckets the maximum number of per-key buckets held in memory at once; values below
  ///     `1` fall back to [#DEFAULT_MAX_BUCKETS]
  public record RateLimit(int maxBuckets) {

    /// Bucket ceiling used when `api.rate-limit.max-buckets` is unset or invalid.
    public static final int DEFAULT_MAX_BUCKETS = 10_000;

    private static final int MIN_MAX_BUCKETS = 1;

    public RateLimit {
      if (maxBuckets < MIN_MAX_BUCKETS) {
        maxBuckets = DEFAULT_MAX_BUCKETS;
      }
    }

    /// @return rate-limit settings using [#DEFAULT_MAX_BUCKETS]
    public static RateLimit defaults() {
      return new RateLimit(DEFAULT_MAX_BUCKETS);
    }
  }
}
