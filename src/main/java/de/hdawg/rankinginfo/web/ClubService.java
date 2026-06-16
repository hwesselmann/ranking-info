package de.hdawg.rankinginfo.web;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.repository.RankingRepository;
import de.hdawg.rankinginfo.web.viewmodel.ClubSummary;
import de.hdawg.rankinginfo.web.viewmodel.PlayerSummaryRow;

/// Builds club-centric view models by aggregating [Ranking] rows for the most recent quarter.
///
/// Two entry points are exposed:
/// - [#searchClubs(String)] returns lightweight summaries (youth/adult player counts) for clubs
///   whose name matches a search term.
/// - [#getClubDetail(String)] returns the full roster for a single club, grouped by adult
///   category (`Herren`/`Damen`) and youth age group (`U12`-`U18`).
///
/// All queries are scoped to the latest available ranking date, as returned by
/// [RankingRepository#findDistinctDatesDesc()].
@Service
@Transactional(readOnly = true)
public class ClubService {

  private static final String AGE_GROUP_OVERALL = "overall";
  private static final String AGE_GROUP_M00 = "m00";
  private static final String AGE_GROUP_W00 = "w00";
  private static final Map<String, String> ADULT_LABELS = Map.of(AGE_GROUP_M00, "Herren", AGE_GROUP_W00, "Damen");

  private final RankingRepository rankingRepository;

  public ClubService(RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
  }

  /// Searches clubs whose name contains `searchTerm` (case-insensitive) and returns one
  /// [ClubSummary] per matching club, sorted alphabetically.
  ///
  /// Each summary reports:
  /// - the number of youth players (`age_group = "overall"`)
  /// - the number of adult players (`age_group = "m00"` or `"w00"`)
  ///
  /// @param searchTerm substring to match against club names
  /// @return matching clubs sorted by name, with youth/adult player counts; empty if no ranking
  ///     data is available yet
  public List<ClubSummary> searchClubs(String searchTerm) {
    var quarter = rankingRepository.findDistinctDatesDesc().stream().findFirst().orElse(null);
    if (quarter == null) return List.of();

    var youthByClub =
        rankingRepository
            .findByDateAndAgeGroupAndClubContaining(quarter, AGE_GROUP_OVERALL, searchTerm)
            .stream()
            .collect(Collectors.groupingBy(Ranking::club));

    var allAdults = new ArrayList<Ranking>();
    allAdults.addAll(
        rankingRepository.findByDateAndAgeGroupAndClubContaining(quarter, AGE_GROUP_M00, searchTerm));
    allAdults.addAll(
        rankingRepository.findByDateAndAgeGroupAndClubContaining(quarter, AGE_GROUP_W00, searchTerm));
    var adultByClub = allAdults.stream().collect(Collectors.groupingBy(Ranking::club));

    return Stream.concat(youthByClub.keySet().stream(), adultByClub.keySet().stream())
        .distinct()
        .sorted()
        .map(
            name ->
                new ClubSummary(
                    name,
                    youthByClub.getOrDefault(name, List.of()).size(),
                    adultByClub.getOrDefault(name, List.of()).size()))
        .toList();
  }

  /// Builds the full player roster for a single club, identified by an exact (case-insensitive)
  /// name match.
  ///
  /// The result is a [LinkedHashMap] preserving insertion order, with keys in the order:
  /// - `Herren` / `Damen` (whichever adult categories the club has players in)
  /// - `U12`, `U14`, `U16`, `U18` (whichever youth age groups the club has players in)
  ///
  /// Youth players are resolved from their `overall` ranking to a specific age-group ranking via
  /// [RankingRepository#findAgeGroupRankingsByDateAndDtbIds(java.time.LocalDate, List)], then
  /// sorted by [Ranking#rankingPosition()].
  ///
  /// @param clubId club name to match exactly (case-insensitive)
  /// @return rows grouped by category/age-group label; empty if no ranking data is available
  public Map<String, List<PlayerSummaryRow>> getClubDetail(String clubId) {
    var quarter = rankingRepository.findDistinctDatesDesc().stream().findFirst().orElse(null);
    var players = new LinkedHashMap<String, List<PlayerSummaryRow>>();
    if (quarter == null) return players;

    for (var entry : ADULT_LABELS.entrySet()) {
      var adults =
          rankingRepository
              .findByDateAndAgeGroupAndClubContaining(quarter, entry.getKey(), clubId)
              .stream()
              .filter(r -> r.club().equalsIgnoreCase(clubId))
              .toList();
      if (!adults.isEmpty()) {
        players.put(entry.getValue(), toPlayerSummaryRows(adults, null));
      }
    }

    var youth =
        rankingRepository
            .findByDateAndAgeGroupAndClubContaining(quarter, AGE_GROUP_OVERALL, clubId)
            .stream()
            .filter(r -> r.club().equalsIgnoreCase(clubId))
            .toList();

    var dtbIds = youth.stream().map(Ranking::dtbId).distinct().toList();
    if (!dtbIds.isEmpty()) {
      var ageGroupRankings =
          rankingRepository
              .findAgeGroupRankingsByDateAndDtbIds(quarter, dtbIds)
              .stream()
              .collect(Collectors.toMap(Ranking::dtbId, r -> r));
      var grouped =
          youth.stream()
              .filter(r -> ageGroupRankings.containsKey(r.dtbId()))
              .collect(
                  Collectors.groupingBy(r -> ageGroupRankings.get(r.dtbId()).ageGroup()));

      for (var ag : List.of("U12", "U14", "U16", "U18")) {
        var group = grouped.get(ag);
        if (group == null) continue;
        var sorted =
            group.stream().sorted(Comparator.comparingInt(Ranking::rankingPosition)).toList();
        players.put(ag, toPlayerSummaryRows(sorted, ag));
      }
    }

    return players;
  }

  private static List<PlayerSummaryRow> toPlayerSummaryRows(
      List<Ranking> rankings, @Nullable String ageGroup) {
    return rankings.stream()
        .map(
            r ->
                new PlayerSummaryRow(
                    r.dtbId(), r.lastname(), r.firstname(), r.rankingPosition(), r.score(), ageGroup))
        .toList();
  }
}
