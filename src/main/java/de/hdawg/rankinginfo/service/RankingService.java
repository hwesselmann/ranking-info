package de.hdawg.rankinginfo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingQueryFilter;
import de.hdawg.rankinginfo.repository.RankingRepository;

@Service
@Transactional(readOnly = true)
public class RankingService {

  private static final String AGE_GROUP_M00 = "m00";
  private static final String AGE_GROUP_W00 = "w00";

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

  @Cacheable("available_quarters")
  public Map<String, List<QuarterEntry>> fetchAvailableQuarters() {
    var years = new TreeMap<String, List<QuarterEntry>>(Comparator.reverseOrder());
    for (var date : rankingRepository.findDistinctDatesDesc()) {
      if (date.getMonthValue() == 12 && date.getDayOfMonth() == 31) continue;
      var adjusted = date.minusDays(1);
      years
          .computeIfAbsent(String.valueOf(adjusted.getYear()), k -> new ArrayList<>())
          .add(new QuarterEntry(adjusted.format(DATE_FORMATTER), date.toString()));
    }
    return years;
  }

  @Cacheable("federations")
  public List<String> fetchFederations() {
    return rankingRepository.findDistinctFederations();
  }

  public Page<Ranking> findFilteredRankings(RankingFilter filter, int page, int perPage) {
    return rankingRepository.findFiltered(buildQueryFilter(filter), page, perPage);
  }

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

    int dtbIdStart = isMale ? 10_000_000 : 20_000_000;
    int dtbIdEnd = isMale ? 19_999_999 : 29_999_999;

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
        filter.yearEnd() && quarter.getMonthValue() == 1,
        fed,
        club,
        null);
  }

  public static String toAgeGroupSlug(String gender, @Nullable String ageGroup) {
    return switch (gender) {
      case "Herren" -> "m00";
      case "Damen" -> "w00";
      case "Junioren" ->
          (ageGroup == null || ageGroup.isBlank())
              ? "overall"
              : "m" + ageGroup.toLowerCase(Locale.ROOT);
      case "Juniorinnen" ->
          (ageGroup == null || ageGroup.isBlank())
              ? "overall"
              : "w" + ageGroup.toLowerCase(Locale.ROOT);
      default -> "overall";
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
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
