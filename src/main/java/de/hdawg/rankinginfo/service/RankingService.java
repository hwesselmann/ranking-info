package de.hdawg.rankinginfo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingQueryFilter;
import de.hdawg.rankinginfo.repository.RankingRepository;

@SuppressWarnings("PMD.LooseCoupling")
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
    var years = new LinkedHashMap<String, List<QuarterEntry>>();
    for (var date : rankingRepository.findDistinctDatesDesc()) {
      if (date.getMonthValue() == 12 && date.getDayOfMonth() == 31) continue;
      var adjusted = date.minusDays(1);
      years
          .computeIfAbsent(String.valueOf(adjusted.getYear()), k -> new ArrayList<>())
          .add(new QuarterEntry(adjusted.format(DATE_FORMATTER), date.toString()));
    }
    return years.entrySet().stream()
        .sorted(
            java.util.Comparator.comparingInt(
                (Map.Entry<String, List<QuarterEntry>> e) -> -Integer.parseInt(e.getKey())))
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
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

    var fed =
        filter.federation() != null && !filter.federation().isBlank()
            ? filter.federation()
            : null;
    var club =
        filter.club() != null && !filter.club().isBlank() ? filter.club() : null;

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

  private static String slugToAgeGroup(String slug) {
    if (AGE_GROUP_M00.equals(slug)) return AGE_GROUP_M00;
    if (AGE_GROUP_W00.equals(slug)) return AGE_GROUP_W00;
    return "U" + slug.substring(2).toUpperCase();
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
