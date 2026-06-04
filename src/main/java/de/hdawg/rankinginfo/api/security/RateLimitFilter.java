package de.hdawg.rankinginfo.api.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;
  private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

  public RateLimitFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/api/v1");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var auth = request.getHeader("Authorization");
    var key = auth != null ? auth : request.getRemoteAddr();
    var bucket = buckets.computeIfAbsent(key, k -> createBucket());

    if (bucket.tryConsume(1)) {
      filterChain.doFilter(request, response);
    } else {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      objectMapper.writeValue(response.getWriter(), Map.of("error", "Too Many Requests"));
    }
  }

  private static Bucket createBucket() {
    return Bucket.builder()
        .addLimit(
            Bandwidth.builder().capacity(1000).refillGreedy(1000, Duration.ofHours(1)).build())
        .build();
  }
}
