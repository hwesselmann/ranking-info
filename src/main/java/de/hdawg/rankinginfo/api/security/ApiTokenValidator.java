package de.hdawg.rankinginfo.api.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import de.hdawg.rankinginfo.config.ApiProperties;

@Component
public final class ApiTokenValidator {

  private final Set<String> validTokens;

  public ApiTokenValidator(ApiProperties apiProperties) {
    var tokens = new HashSet<String>();
    apiProperties.tokens().stream().filter(t -> !t.isBlank()).forEach(tokens::add);
    var env = System.getenv("API_BEARER_TOKEN");
    if (env != null && !env.isBlank()) {
      tokens.add(env);
    }
    this.validTokens = Set.copyOf(tokens);
  }

  public boolean isValid(String token) {
    var tokenBytes = token.getBytes(StandardCharsets.UTF_8);
    boolean valid = false;
    for (var t : validTokens) {
      if (MessageDigest.isEqual(tokenBytes, t.getBytes(StandardCharsets.UTF_8))) {
        valid = true;
      }
    }
    return valid;
  }
}
