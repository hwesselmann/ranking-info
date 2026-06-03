package de.hdawg.rankinginfo.service;

import org.springframework.lang.Nullable;

public record RankingFilter(
    String quarter,
    String ageGroupSlug,
    @Nullable String ageGroupOptions,
    @Nullable String federation,
    @Nullable String club,
    boolean yearEnd) {

  public RankingFilter withQuarter(String quarter) {
    return new RankingFilter(quarter, ageGroupSlug, ageGroupOptions, federation, club, yearEnd);
  }
}
