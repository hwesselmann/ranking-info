package de.hdawg.rankinginfo.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class ApiPropertiesTest {

  private static ApiProperties bind(Map<String, Object> properties) {
    return new Binder(new MapConfigurationPropertySource(properties))
        .bind("api", ApiProperties.class)
        .orElseThrow(() -> new AssertionError("api properties did not bind"));
  }

  @Test
  @DisplayName("binds tokens and the rate-limit bucket ceiling from configuration")
  void bindsTokensAndRateLimit() {
    var properties =
        bind(
            Map.of(
                "api.tokens[0]", "token-a",
                "api.rate-limit.max-buckets", "512"));

    assertThat(properties.tokens()).containsExactly("token-a");
    assertThat(properties.rateLimit().maxBuckets()).isEqualTo(512);
  }

  @Test
  @DisplayName("applies the default bucket ceiling when rate-limit config is absent")
  void appliesDefaultBucketCeilingWhenAbsent() {
    var properties = bind(Map.of("api.tokens[0]", "token-a"));

    assertThat(properties.rateLimit().maxBuckets())
        .isEqualTo(ApiProperties.RateLimit.DEFAULT_MAX_BUCKETS);
  }

  @Test
  @DisplayName("rejects a non-positive bucket ceiling in favour of the default")
  void rejectsNonPositiveBucketCeiling() {
    var properties = bind(Map.of("api.rate-limit.max-buckets", "0"));

    assertThat(properties.rateLimit().maxBuckets())
        .isEqualTo(ApiProperties.RateLimit.DEFAULT_MAX_BUCKETS);
  }

  @Test
  @DisplayName("treats missing tokens as an empty list")
  void treatsMissingTokensAsEmptyList() {
    var properties = bind(Map.of("api.rate-limit.max-buckets", "512"));

    assertThat(properties.tokens()).isEqualTo(List.of());
  }
}
