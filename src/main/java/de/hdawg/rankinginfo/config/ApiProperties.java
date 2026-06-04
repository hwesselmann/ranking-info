package de.hdawg.rankinginfo.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api")
public record ApiProperties(List<String> tokens) {
  public ApiProperties {
    tokens = tokens == null ? List.of() : List.copyOf(tokens);
  }
}
