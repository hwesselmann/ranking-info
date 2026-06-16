package de.hdawg.rankinginfo.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hdawg.rankinginfo.domain.RankingCoding;
import de.hdawg.rankinginfo.repository.RankingRepository;

/// Builds per-federation player count statistics for the most recent quarter.
///
/// [#buildFederationData()] is the sole entry point. It returns a map keyed by federation display
/// name (e.g. `Bayern`), with each value a map keyed by age-group/gender label (e.g. `U16m`,
/// `m00`) to player count. Federation abbreviations from the data (e.g. `BTV`) are translated to
/// display names via [#FEDERATION_NAMES]; unknown abbreviations pass through unchanged.
///
/// The result is cached under `federation_stats` and is scoped to the latest available ranking
/// date, as returned by [RankingRepository#findDistinctDatesDesc()].
@Service
@Transactional(readOnly = true)
public class FederationService {

  private static final String GENDER_MALE = "m";
  private static final String GENDER_FEMALE = "w";
  private static final String AGE_GROUP_M00 = "m00";
  private static final String AGE_GROUP_W00 = "w00";

  private static final Map<String, String> FEDERATION_NAMES =
      Map.ofEntries(
          Map.entry("BAD", "Baden"),
          Map.entry("BB", "Berlin-Brandenburg"),
          Map.entry("BTV", "Bayern"),
          Map.entry("HAM", "Hamburg"),
          Map.entry("HTV", "Hessen"),
          Map.entry("RPF", "Rheinland-Pfalz"),
          Map.entry("SLH", "Schleswig-Holstein"),
          Map.entry("STB", "Saarland"),
          Map.entry("STV", "Sachsen"),
          Map.entry("TMV", "Mecklenburg-Vorpommern"),
          Map.entry("TNB", "Niedersachsen-Bremen"),
          Map.entry("TSA", "Sachsen-Anhalt"),
          Map.entry("TTV", "Thüringen"),
          Map.entry("TVM", "Mittelrhein"),
          Map.entry("TVN", "Niederrhein"),
          Map.entry("WTB", "Württemberg"),
          Map.entry("WTV", "Westfalen"));

  private final RankingRepository rankingRepository;

  public FederationService(RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
  }

  /// Aggregates player counts by federation for the latest ranking quarter.
  ///
  /// For each gender, youth players are counted per age group (via
  /// [RankingRepository#countYouthByFederationAndAgeGroup(java.time.LocalDate, int, int)],
  /// keyed by DTB ID range) and adult players are counted per adult category (`m00`/`w00`, via
  /// [RankingRepository#countAdultByFederation(java.time.LocalDate, String)]).
  ///
  /// @return a map from federation display name to a map of age-group/gender label to player
  ///     count; empty if no ranking data is available yet
  @Cacheable("federation_stats")
  public Map<String, Map<String, Integer>> buildFederationData() {
    var quarter = rankingRepository.findDistinctDatesDesc().stream().findFirst().orElse(null);
    var federations = new LinkedHashMap<String, Map<String, Integer>>();
    if (quarter == null) return federations;

    for (var gender : new String[] {GENDER_MALE, GENDER_FEMALE}) {
      int dtbIdStart =
          GENDER_MALE.equals(gender)
              ? RankingCoding.MALE_DTB_ID_START
              : RankingCoding.FEMALE_DTB_ID_START;
      int dtbIdEnd =
          GENDER_MALE.equals(gender)
              ? RankingCoding.MALE_DTB_ID_END
              : RankingCoding.FEMALE_DTB_ID_END;
      for (var row :
          rankingRepository.countYouthByFederationAndAgeGroup(quarter, dtbIdStart, dtbIdEnd)) {
        var fed = FEDERATION_NAMES.getOrDefault(row.federation(), row.federation());
        federations
            .computeIfAbsent(fed, k -> new LinkedHashMap<>())
            .put(row.ageGroup() + gender, (int) row.count());
      }
    }
    for (var ag : new String[] {AGE_GROUP_M00, AGE_GROUP_W00}) {
      for (var row : rankingRepository.countAdultByFederation(quarter, ag)) {
        var fed = FEDERATION_NAMES.getOrDefault(row.federation(), row.federation());
        federations.computeIfAbsent(fed, k -> new LinkedHashMap<>()).put(ag, (int) row.count());
      }
    }
    return federations;
  }
}
