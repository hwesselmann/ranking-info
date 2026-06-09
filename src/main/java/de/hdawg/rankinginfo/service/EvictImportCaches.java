package de.hdawg.rankinginfo.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cache.annotation.CacheEvict;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@CacheEvict(
    cacheNames = {
      "available_quarters",
      "available_dates",
      "federations",
      "player_counts",
      "max_imported_at",
      "federation_stats"
    },
    allEntries = true)
public @interface EvictImportCaches {}
