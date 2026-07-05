package de.hdawg.rankinginfo.api.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.hdawg.rankinginfo.config.ApiProperties;

class ApiTokenValidatorTest {

  @BeforeEach
  void setUp() {
    System.setProperty("API_BEARER_TOKEN", "");
  }

  @AfterEach
  void tearDown() {
    System.clearProperty("API_BEARER_TOKEN");
  }

  @Test
  @DisplayName("accepts a configured token")
  void acceptsConfiguredToken() {
    var validator = new ApiTokenValidator(new ApiProperties(List.of("valid-token")));

    assertTrue(validator.isValid("valid-token"));
  }

  @Test
  @DisplayName("rejects an unconfigured token")
  void rejectsUnconfiguredToken() {
    var validator = new ApiTokenValidator(new ApiProperties(List.of("valid-token")));

    assertFalse(validator.isValid("wrong-token"));
  }

  @Test
  @DisplayName("ignores blank tokens in configuration")
  void ignoresBlankTokensInConfiguration() {
    var validator = new ApiTokenValidator(new ApiProperties(List.of("  ", "", "valid-token")));

    assertFalse(validator.isValid("  "));
    assertTrue(validator.isValid("valid-token"));
  }

  @Test
  @DisplayName("accepts any token from a multi-token configuration")
  void acceptsAnyTokenFromMultiTokenConfig() {
    var validator = new ApiTokenValidator(new ApiProperties(List.of("token-a", "token-b", "token-c")));

    assertTrue(validator.isValid("token-a"));
    assertTrue(validator.isValid("token-b"));
    assertTrue(validator.isValid("token-c"));
  }
}
