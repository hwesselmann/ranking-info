package de.hdawg.rankinginfo.api.security;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

public final class BearerTokenFilter extends OncePerRequestFilter {

  private final ApiTokenValidator tokenValidator;
  private final ObjectMapper objectMapper;

  public BearerTokenFilter(ApiTokenValidator tokenValidator, ObjectMapper objectMapper) {
    this.tokenValidator = tokenValidator;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var authHeader = request.getHeader("Authorization");
    var token = authHeader != null ? authHeader.replaceFirst("^Bearer ", "") : null;
    if (token == null || token.isBlank() || !tokenValidator.isValid(token)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      objectMapper.writeValue(response.getWriter(), Map.of("error", "Unauthorized"));
      return;
    }
    filterChain.doFilter(request, response);
  }
}
