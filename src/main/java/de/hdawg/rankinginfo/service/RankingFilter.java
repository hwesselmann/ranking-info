package de.hdawg.rankinginfo.service;

import org.jspecify.annotations.Nullable;

/// Filter criteria gathered from a web/API request for browsing rankings, translated by
/// [RankingService] into a query the repository layer understands.
///
/// @param quarter the selected ranking quarter, as an ISO date string
/// @param ageGroupSlug the selected age-group slug (e.g. `m00`, `mu18`)
/// @param ageGroupOptions for junior age-group slugs, selects which ranking variant to query:
///     `only_yob` selects the single-birth-year ranking, `include_younger` selects the
///     cumulative (age-and-younger) ranking; `null` selects the variant matching the slug's age
///     number parity (single-birth-year for odd ages, double-cohort bracket for even ages).
///     Ignored for adult age groups (`m00`/`w00`)
/// @param federation federation abbreviation to filter by, or `null`/blank for all
/// @param club club name to filter by, or `null`/blank for all
/// @param yearEnd whether to use the year-end ranking instead of the regular quarterly one
public record RankingFilter(
    String quarter,
    String ageGroupSlug,
    @Nullable String ageGroupOptions,
    @Nullable String federation,
    @Nullable String club,
    boolean yearEnd) {

  /// Returns a copy of this filter with `quarter` replaced; all other criteria are unchanged.
  ///
  /// @param quarter the new quarter value
  /// @return the updated filter
  public RankingFilter withQuarter(String quarter) {
    return new RankingFilter(quarter, ageGroupSlug, ageGroupOptions, federation, club, yearEnd);
  }
}
