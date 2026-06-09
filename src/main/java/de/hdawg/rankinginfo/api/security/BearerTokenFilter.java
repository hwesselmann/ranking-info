package de.hdawg.rankinginfo.api.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

public class BearerTokenFilter extends OncePerRequestFilter {

  private final Set<String> validTokens;
  private final ObjectMapper objectMapper;

  public BearerTokenFilter(List<String> configuredTokens, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    var tokens = new HashSet<String>();
    configuredTokens.stream().filter(t -> !t.isBlank()).forEach(tokens::add);
    var env = System.getenv("API_BEARER_TOKEN");
    if (env != null && !env.isBlank()) {
      tokens.add(env);
    }
    this.validTokens = Set.copyOf(tokens);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var authHeader = request.getHeader("Authorization");
    var token = authHeader != null ? authHeader.replaceFirst("^Bearer ", "") : null;
    if (token == null || token.isBlank() || !isValidToken(token)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      objectMapper.writeValue(response.getWriter(), Map.of("error", "Unauthorized"));
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean isValidToken(String token) {
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
