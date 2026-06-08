package de.hdawg.rankinginfo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "import")
public record ImportProperties(String folder) {}
