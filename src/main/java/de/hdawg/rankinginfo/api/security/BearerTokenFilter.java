package de.hdawg.rankinginfo.api.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class BearerTokenFilter extends OncePerRequestFilter {

  private final List<String> configuredTokens;
  private final ObjectMapper objectMapper;
  private final String envToken;

  public BearerTokenFilter(List<String> configuredTokens, ObjectMapper objectMapper) {
    this.configuredTokens = List.copyOf(configuredTokens);
    this.objectMapper = objectMapper;
    this.envToken = System.getenv("API_BEARER_TOKEN");
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
      objectMapper.writeValue(response.getWriter(), java.util.Map.of("error", "Unauthorized"));
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean isValidToken(String token) {
    var validTokens = new HashSet<String>();
    configuredTokens.stream().filter(t -> !t.isBlank()).forEach(validTokens::add);
    if (envToken != null && !envToken.isBlank()) {
      validTokens.add(envToken);
    }
    return validTokens.contains(token);
  }
}
