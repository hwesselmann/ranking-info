package de.hdawg.rankinginfo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import de.hdawg.rankinginfo.api.security.BearerTokenFilter;
import de.hdawg.rankinginfo.api.security.RateLimitFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({ApiProperties.class, ImportProperties.class})
public class SecurityConfig {

  @Bean
  @Order(1)
  public SecurityFilterChain apiSecurityFilterChain(
      HttpSecurity http, ApiProperties apiProperties, ObjectMapper objectMapper) throws Exception {
    var rateLimitFilter = new RateLimitFilter(objectMapper);
    var bearerFilter = new BearerTokenFilter(apiProperties.tokens(), objectMapper);

    return http.securityMatcher("/api/v1/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(bearerFilter, RateLimitFilter.class)
        .build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .build();
  }
}
