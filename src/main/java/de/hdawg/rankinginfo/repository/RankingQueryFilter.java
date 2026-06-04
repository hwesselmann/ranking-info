package de.hdawg.rankinginfo.repository;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;

public record RankingQueryFilter(
    LocalDate date,
    String ageGroup,
    int dtbIdStart,
    int dtbIdEnd,
    boolean yobRanking,
    boolean ageGroupRanking,
    boolean yearEndRanking,
    @Nullable String federation,
    @Nullable String club,
    @Nullable List<Integer> dtbIds) {

  @SuppressWarnings("PMD.NullAssignment")
  public RankingQueryFilter {
    dtbIds = dtbIds != null ? List.copyOf(dtbIds) : null;
  }

  public RankingQueryFilter withDtbIds(@Nullable List<Integer> dtbIds) {
    return new RankingQueryFilter(
        date, ageGroup, dtbIdStart, dtbIdEnd,
        yobRanking, ageGroupRanking, yearEndRanking,
        federation, club, dtbIds);
  }
}
