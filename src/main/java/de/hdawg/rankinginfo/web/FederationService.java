package de.hdawg.rankinginfo.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hdawg.rankinginfo.domain.RankingCoding;
import de.hdawg.rankinginfo.repository.RankingRepository;

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
