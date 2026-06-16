package de.hdawg.rankinginfo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.domain.RankingCoding;
import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingQueryFilter;
import de.hdawg.rankinginfo.repository.RankingRepository;

/// Business-logic facade over [RankingRepository] for browsing rankings: translates the
/// web/API-facing [RankingFilter] into a [RankingQueryFilter] the repository understands, and
/// exposes supporting lookups (available quarters, federations, previous-quarter positions, last
/// import timestamp).
///
/// Age-group slugs (e.g. `m00`, `mu18`) are translated to/from stored `age_group` values by
/// [#toAgeGroupSlug(String, String)] and [#slugToAgeGroup(String)].
@Service
@Transactional(readOnly = true)
public class RankingService {

  private static final String AGE_GROUP_M00 = "m00";
  private static final String AGE_GROUP_W00 = "w00";
  private static final String AGE_GROUP_OVERALL = "overall";

  private record YobAgeFlags(boolean yobRanking, boolean ageGroupRanking) {}

  private static final Map<String, YobAgeFlags> AGE_GROUP_OPTION_FILTERS =
      Map.of(
          "only_yob", new YobAgeFlags(true, false),
          "include_younger", new YobAgeFlags(false, false));

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  private final RankingRepository rankingRepository;
  private final ImportHistoryRepository importHistoryRepository;

  public RankingService(
      RankingRepository rankingRepository, ImportHistoryRepository importHistoryRepository) {
    this.rankingRepository = rankingRepository;
    this.importHistoryRepository = importHistoryRepository;
  }

  /// Lists every quarter that has ranking data, grouped by year.
  ///
  /// Each stored ranking date is the first day of the following quarter (e.g. data for Q1 is
  /// dated April 1st); entries are adjusted back by one day before being grouped and formatted,
  /// except the synthetic December 31st year-end ranking date, which is skipped entirely.
  ///
  /// @return years (as strings, descending) mapped to their available quarters
  @Cacheable("available_quarters")
  public Map<String, List<QuarterEntry>> fetchAvailableQuarters() {
    var years = new TreeMap<String, List<QuarterEntry>>(Comparator.reverseOrder());
    for (var date : rankingRepository.findDistinctDatesDesc()) {
      if (date.getMonthValue() == Month.DECEMBER.getValue() && date.getDayOfMonth() == 31) continue;
      var adjusted = date.minusDays(1);
      years
          .computeIfAbsent(String.valueOf(adjusted.getYear()), k -> new ArrayList<>())
          .add(new QuarterEntry(adjusted.format(DATE_FORMATTER), date.toString()));
    }
    return years;
  }

  /// Lists every distinct federation abbreviation present in the stored rankings.
  ///
  /// @return distinct federation abbreviations
  @Cacheable("federations")
  public List<String> fetchFederations() {
    return rankingRepository.findDistinctFederations();
  }

  /// Fetches one page of rankings matching `filter`.
  ///
  /// @param filter the web/API-facing filter criteria
  /// @param page zero-based page index
  /// @param perPage page size
  /// @return the matching page of rankings
  public Page<Ranking> findFilteredRankings(RankingFilter filter, int page, int perPage) {
    return rankingRepository.findFiltered(buildQueryFilter(filter), page, perPage);
  }

  /// Looks up each given player's ranking position in the quarter immediately preceding
  /// `filter`'s quarter, for computing position-change indicators.
  ///
  /// @param filter the filter whose quarter to look up the *previous* quarter for; its other
  ///     criteria (age group, federation, club) are reused as-is
  /// @param dtbIds the players to look up; returns an empty map if this is empty
  /// @return each looked-up player's previous-quarter ranking position, keyed by DTB ID; empty
  ///     if `filter`'s quarter has no preceding quarter with data
  public Map<Integer, Integer> findPreviousPositions(RankingFilter filter, List<Integer> dtbIds) {
    if (dtbIds.isEmpty()) return Map.of();
    var quarterDate = LocalDate.parse(filter.quarter());
    var prevDate =
        rankingRepository.findDistinctDatesDesc().stream()
            .filter(d -> d.isBefore(quarterDate))
            .findFirst()
            .orElse(null);
    if (prevDate == null) return Map.of();
    var prevFilter =
        buildQueryFilter(filter.withQuarter(prevDate.toString())).withDtbIds(dtbIds);
    return rankingRepository.findFiltered(prevFilter).stream()
        .collect(Collectors.toMap(Ranking::dtbId, Ranking::rankingPosition));
  }

  /// Returns the timestamp of the most recent successful import, if any.
  ///
  /// @return the latest import timestamp, or `null` if nothing has been imported yet
  @Cacheable("max_imported_at")
  @Nullable
  public LocalDateTime maxImportedAt() {
    return importHistoryRepository.findMaxImportedAt();
  }

  private static RankingQueryFilter buildQueryFilter(RankingFilter filter) {
    var quarter = LocalDate.parse(filter.quarter());
    var slug = filter.ageGroupSlug();
    var ageGroup = slugToAgeGroup(slug);
    var isMale = slug.startsWith("m");
    var isAdult = AGE_GROUP_M00.equals(ageGroup) || AGE_GROUP_W00.equals(ageGroup);

    int dtbIdStart = isMale ? RankingCoding.MALE_DTB_ID_START : RankingCoding.FEMALE_DTB_ID_START;
    int dtbIdEnd = isMale ? RankingCoding.MALE_DTB_ID_END : RankingCoding.FEMALE_DTB_ID_END;

    boolean yobRanking;
    boolean ageGroupRanking;
    if (isAdult) {
      yobRanking = false;
      ageGroupRanking = false;
    } else if (filter.ageGroupOptions() == null) {
      int n = parseAgeGroupNumber(ageGroup);
      yobRanking = (n % 2 != 0);
      ageGroupRanking = (n % 2 == 0);
    } else {
      var flags =
          AGE_GROUP_OPTION_FILTERS.getOrDefault(
              filter.ageGroupOptions(), new YobAgeFlags(false, false));
      yobRanking = flags.yobRanking();
      ageGroupRanking = flags.ageGroupRanking();
    }

    var fedValue = filter.federation();
    var fed = fedValue != null && !fedValue.isBlank() ? fedValue : null;
    var clubValue = filter.club();
    var club = clubValue != null && !clubValue.isBlank() ? clubValue : null;

    return new RankingQueryFilter(
        quarter,
        ageGroup,
        dtbIdStart,
        dtbIdEnd,
        yobRanking,
        ageGroupRanking,
        filter.yearEnd() && quarter.getMonthValue() == Month.JANUARY.getValue(),
        fed,
        club,
        null);
  }

  /// Builds the URL/API age-group slug (e.g. `m00`, `mu18`) for a gender category and, for
  /// junior categories, an optional age-group label.
  ///
  /// @param gender one of `Herren`, `Damen`, `Junioren`, `Juniorinnen`; anything else yields
  ///     `overall`
  /// @param ageGroup for `Junioren`/`Juniorinnen`, the age-group label (e.g. `U18`); `null` or
  ///     blank yields the `overall` slug for that gender
  /// @return the age-group slug
  public static String toAgeGroupSlug(String gender, @Nullable String ageGroup) {
    return switch (gender) {
      case "Herren" -> "m00";
      case "Damen" -> "w00";
      case "Junioren" ->
          (ageGroup == null || ageGroup.isBlank())
              ? AGE_GROUP_OVERALL
              : "m" + ageGroup.toLowerCase(Locale.ROOT);
      case "Juniorinnen" ->
          (ageGroup == null || ageGroup.isBlank())
              ? AGE_GROUP_OVERALL
              : "w" + ageGroup.toLowerCase(Locale.ROOT);
      default -> AGE_GROUP_OVERALL;
    };
  }

  private static String slugToAgeGroup(String slug) {
    if (AGE_GROUP_M00.equals(slug)) return AGE_GROUP_M00;
    if (AGE_GROUP_W00.equals(slug)) return AGE_GROUP_W00;
    return "U" + slug.substring(2).toUpperCase(Locale.ROOT);
  }

  private static int parseAgeGroupNumber(String ageGroup) {
    if (!ageGroup.startsWith("U")) return 0;
    try {
      return Integer.parseInt(ageGroup.substring(1));
    } catch (NumberFormatException _) {
      return 0;
    }
  }
}
