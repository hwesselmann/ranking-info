package de.hdawg.rankinginfo.web;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

  /// Shortest club-search term accepted. A single character matches almost every club, which
  /// would turn one request into a scan of the whole quarter.
  public static final int MIN_SEARCH_TERM_LENGTH = 2;

  private static final String TOO_SHORT_MESSAGE =
      "Bitte mindestens " + MIN_SEARCH_TERM_LENGTH + " Zeichen eingeben.";

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
  /// Counting happens in the database. A very broad term therefore costs one row per club rather
  /// than one row per player; `searchTerm` must still be at least [#MIN_SEARCH_TERM_LENGTH]
  /// characters long so a single character cannot scan the whole quarter.
  ///
  /// @param searchTerm substring to match against club names
  /// @return matching clubs sorted by name, with youth/adult player counts; empty if no ranking
  ///     data is available yet
  /// @throws IllegalArgumentException if `searchTerm` is shorter than [#MIN_SEARCH_TERM_LENGTH]
  ///     characters after trimming
  public List<ClubSummary> searchClubs(String searchTerm) {
    var term = searchTerm == null ? "" : searchTerm.trim();
    if (term.length() < MIN_SEARCH_TERM_LENGTH) {
      throw new IllegalArgumentException(TOO_SHORT_MESSAGE);
    }

    var quarter = rankingRepository.findLatestDate();
    if (quarter == null) return List.of();

    return rankingRepository.countPlayersByClubContaining(quarter, term).stream()
        .map(c -> new ClubSummary(c.club(), (int) c.youthCount(), (int) c.adultCount()))
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
    var quarter = rankingRepository.findLatestDate();
    var players = new LinkedHashMap<String, List<PlayerSummaryRow>>();
    if (quarter == null) return players;

    var all = rankingRepository.findByDateAndAgeGroupsAndExactClub(
        quarter, List.of(AGE_GROUP_OVERALL, AGE_GROUP_M00, AGE_GROUP_W00), clubId);

    for (var entry : ADULT_LABELS.entrySet()) {
      var adults = all.stream().filter(r -> r.ageGroup().equals(entry.getKey())).toList();
      if (!adults.isEmpty()) {
        players.put(entry.getValue(), toPlayerSummaryRows(adults, null));
      }
    }

    var youth = all.stream().filter(r -> AGE_GROUP_OVERALL.equals(r.ageGroup())).toList();

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
