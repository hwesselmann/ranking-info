package de.hdawg.rankinginfo.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hdawg.rankinginfo.domain.Club;
import de.hdawg.rankinginfo.domain.Player;
import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.repository.RankingRepository;
import de.hdawg.rankinginfo.service.AgeGroupResolver;
import de.hdawg.rankinginfo.web.viewmodel.AgeGroupTimeSeries;
import de.hdawg.rankinginfo.web.viewmodel.CompleteRankingRow;
import de.hdawg.rankinginfo.web.viewmodel.CurrentRankingRow;
import de.hdawg.rankinginfo.web.viewmodel.DiagramDataView;
import de.hdawg.rankinginfo.web.viewmodel.ScoreTimeSeries;

/// Builds the [PlayerProfile] view model for a single player, assembling current rankings, the
/// full ranking history, and diagram data from raw [Ranking] rows.
///
/// [#loadProfile(int)] is the orchestrating entry point; the other public methods are the
/// individual build steps it composes, exposed so they can be tested in isolation.
@SuppressWarnings({"PMD.LooseCoupling", "PMD.AvoidLiteralsInIfCondition"})
@Service
public class PlayerProfileService {

  /// View model for a player's profile page.
  ///
  /// @param player player display data, including known club history
  /// @param currentRankings ranking rows for the most recent quarter
  /// @param completeRankings the full per-quarter ranking history
  /// @param allTimeDiagram diagram series covering the player's entire history
  /// @param recent12mDiagram diagram series covering roughly the last 12 months (last 4 quarters)
  /// @param availableData quarters with ranking data, grouped by year, most recent year first
  public record PlayerProfile(
      Player player,
      List<CurrentRankingRow> currentRankings,
      List<CompleteRankingRow> completeRankings,
      DiagramDataView allTimeDiagram,
      DiagramDataView recent12mDiagram,
      Map<Integer, Set<String>> availableData) {

    public PlayerProfile {
      currentRankings = List.copyOf(currentRankings);
      completeRankings = List.copyOf(completeRankings);
      var copiedMap = new TreeMap<Integer, Set<String>>(Comparator.reverseOrder());
      availableData.forEach((k, v) -> copiedMap.put(k, Set.copyOf(v)));
      availableData = Collections.unmodifiableMap(copiedMap);
    }
  }

  @Nullable private final RankingRepository rankingRepository;

  public PlayerProfileService(@Nullable RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
  }

  private static final Set<String> SPECIAL_SCORES = Set.of("0,0", "PR", "Einst.");

  private static final Map<Integer, String> QUARTER_MAP =
      Map.of(1, "Q1", 4, "Q2", 7, "Q3", 10, "Q4");
  private static final List<String> ADULT_AGE_GROUPS = List.of("m00", "w00");
  private static final String AGE_GROUP_OVERALL = "overall";
  private static final String AKTIVE_LABEL = "Aktive";
  private static final List<String> DIAGRAM_ORDER = List.of("U12", "U14", "U16", "U18", AKTIVE_LABEL);
  private static final int DATE_PARTS_SIZE = 2;
  private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  /// Loads the full profile for a player, or [Optional#empty()] if no ranking data exists for
  /// them.
  ///
  /// @param dtbId the player's DTB ID
  /// @return the assembled profile, or empty if the player has no ranking rows
  /// @throws IllegalStateException if no [RankingRepository] was injected
  @Transactional(readOnly = true)
  public Optional<PlayerProfile> loadProfile(int dtbId) {
    var repo = rankingRepository;
    if (repo == null) throw new IllegalStateException("RankingRepository not injected");

    var rawRankings =
        repo.findByDtbIdAndYobRankingFalseAndAgeGroupRankingFalseAndYearEndRankingFalseOrderByDateDescAgeGroupAsc(
            dtbId);
    if (rawRankings.isEmpty()) return Optional.empty();

    var current = rawRankings.getFirst();
    var clubs = buildClubs(rawRankings);
    var player =
        new Player(
            dtbId,
            current.lastname(),
            current.firstname(),
            current.nationality(),
            current.club(),
            current.federation(),
            clubs);

    var fullRankings =
        repo.findByDtbIdAndYearEndRankingFalseOrderByDateAscAgeGroupAsc(dtbId);
    var allDates = repo.findDistinctDatesDesc();
    var currentQuarter = allDates.isEmpty() ? null : allDates.getFirst();
    var previousQuarter = allDates.size() > 1 ? allDates.get(1) : null;
    var recent4Dates = Set.copyOf(allDates.stream().limit(4).toList());

    var ageGroupRankings = currentQuarter != null
        ? repo.findAgeGroupRankingsByDateAndDtbIds(currentQuarter, List.of(dtbId))
        : List.<Ranking>of();
    var currentRankings = buildCurrentRankings(dtbId, rawRankings, currentQuarter, previousQuarter, ageGroupRankings);
    var allTimeDiagram = buildDiagramData(fullRankings);
    var recent12mDiagram =
        buildDiagramData(
            fullRankings.stream().filter(r -> recent4Dates.contains(r.date())).toList());
    var completeRankings = buildCompleteRankings(fullRankings);
    var availableData = buildAvailableData(completeRankings);

    return Optional.of(
        new PlayerProfile(
            player, currentRankings, completeRankings, allTimeDiagram, recent12mDiagram,
            availableData));
  }

  /// Derives the distinct clubs a player has played for from their date-descending ranking
  /// rows, in order of most recent appearance.
  ///
  /// @param rankingsDateDesc the player's raw ranking rows, ordered newest first
  /// @return distinct clubs, most recently played for first
  static List<Club> buildClubs(List<Ranking> rankingsDateDesc) {
    var map = new LinkedHashMap<String, Club>();
    rankingsDateDesc.forEach(r -> map.putIfAbsent(r.club(), new Club(r.club(), r.federation())));
    return List.copyOf(map.values());
  }

  /// Builds the player's per-age-group ranking rows for `currentQuarter`, computing
  /// position/score deltas against `previousQuarter` and marking which age group is the
  /// player's "current" (single) youth age group.
  ///
  /// @param dtbId player's DTB ID, used to resolve youth age-group membership
  /// @param rawRankings all raw ranking rows for the player, across any quarter
  /// @param currentQuarter the quarter to build rows for; returns an empty list if `null`
  /// @param previousQuarter the prior quarter to diff against, or `null` if unavailable
  /// @param ageGroupRankings the player's age-group-bracket rankings for `currentQuarter`
  /// @return current-quarter ranking rows, adult categories sorted first, then youth age groups
  ///     ascending
  public List<CurrentRankingRow> buildCurrentRankings(
      int dtbId,
      List<Ranking> rawRankings,
      @Nullable LocalDate currentQuarter,
      @Nullable LocalDate previousQuarter,
      List<Ranking> ageGroupRankings) {
    if (currentQuarter == null) return List.of();

    boolean junior = AgeGroupResolver.isJunior(dtbId, currentQuarter);
    String singleAgeGroup = junior ? AgeGroupResolver.currentSingleAgeGroup(dtbId, currentQuarter) : null;
    OptionalInt doubleOpt = junior ? AgeGroupResolver.currentDoubleAgeGroup(dtbId, currentQuarter) : OptionalInt.empty();
    String doubleAgeGroup = doubleOpt.isPresent() ? "U" + doubleOpt.getAsInt() : null;

    Integer agRankingPos = null;
    if (doubleAgeGroup != null) {
      String dag = doubleAgeGroup;
      agRankingPos = ageGroupRankings.stream()
          .filter(r -> dag.equals(r.ageGroup()))
          .map(Ranking::rankingPosition)
          .findFirst()
          .orElse(null);
    }
    Integer finalAgRankingPos = agRankingPos;
    String finalSingle = singleAgeGroup;
    String finalDouble = doubleAgeGroup;

    return rawRankings.stream()
        .filter(r -> r.date().equals(currentQuarter) && !AGE_GROUP_OVERALL.equals(r.ageGroup()))
        .map(r -> {
          var prev = previousQuarter == null ? null : rawRankings.stream()
              .filter(p -> p.date().equals(previousQuarter) && p.ageGroup().equals(r.ageGroup()))
              .findFirst()
              .orElse(null);
          boolean isAdult = ADULT_AGE_GROUPS.contains(r.ageGroup());
          boolean isCurrent = !isAdult && r.ageGroup().equals(finalSingle);
          boolean showScore = isAdult || isCurrent;
          Integer agPos = r.ageGroup().equals(finalDouble) ? finalAgRankingPos : null;
          return new CurrentRankingRow(
              r.ageGroup(),
              r.rankingPosition(),
              r.score(),
              computePositionChange(prev, r),
              showScore ? computeScoreChange(prev, r) : null,
              isCurrent,
              showScore,
              agPos);
        })
        .sorted(Comparator.comparingInt(row -> currentRankingsSortKey(row, finalSingle)))
        .toList();
  }

  private static int currentRankingsSortKey(CurrentRankingRow row, @Nullable String currentSingle) {
    if (ADULT_AGE_GROUPS.contains(row.ageGroup())) return 0;
    if (row.ageGroup().equals(currentSingle)) return 1;
    return Integer.parseInt(row.ageGroup().substring(1)) + 1;
  }

  /// Builds one row per quarter the player has full (non age-group-bracket, non
  /// year-of-birth) rankings for, mapping each row's age groups to ranking positions.
  ///
  /// @param rankings the player's ranking rows, across any quarter
  /// @return rows ordered by quarter, most recent first; quarters that don't map to `Q1`-`Q4`
  ///     are skipped
  public List<CompleteRankingRow> buildCompleteRankings(List<Ranking> rankings) {
    var byDate =
        rankings.stream()
            .filter(r -> !AGE_GROUP_OVERALL.equals(r.ageGroup()) && !r.ageGroupRanking() && !r.yobRanking())
            .collect(
                Collectors.groupingBy(
                    Ranking::date,
                    () -> new TreeMap<>(Comparator.reverseOrder()),
                    Collectors.toList()));
    var result = new ArrayList<CompleteRankingRow>();
    for (var entry : byDate.entrySet()) {
      var date = entry.getKey();
      var rows = entry.getValue();
      var quarter = QUARTER_MAP.get(date.getMonthValue());
      if (quarter == null) continue;
      var ageGroupPositions = new LinkedHashMap<String, Integer>();
      for (var r : rows) {
        var key = ADULT_AGE_GROUPS.contains(r.ageGroup()) ? AKTIVE_LABEL : r.ageGroup();
        ageGroupPositions.put(key, r.rankingPosition());
      }
      result.add(
          new CompleteRankingRow(
              date.getYear() + "/" + quarter,
              rows.isEmpty() ? "" : rows.getFirst().score(),
              ageGroupPositions));
    }
    return result;
  }

  /// Builds diagram time series (ranking position per age group, plus score) from rankings,
  /// considering only adult or age-group-bracket rows; year-of-birth rows are excluded.
  ///
  /// @param rankings ranking rows to chart, across any quarter
  /// @return diagram data with one position series per age group present (ordered per
  ///     [#DIAGRAM_ORDER]) and a single score series; both are empty if no row qualifies
  public DiagramDataView buildDiagramData(List<Ranking> rankings) {
    var general =
        rankings.stream()
            .filter(r -> !r.yobRanking() && (ADULT_AGE_GROUPS.contains(r.ageGroup()) || r.ageGroupRanking()))
            .sorted(Comparator.comparing(Ranking::date))
            .toList();
    if (general.isEmpty()) {
      return new DiagramDataView(List.of(), List.of());
    }
    var positions = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
    var scores = new LinkedHashMap<String, String>();
    for (var r : general) {
      String groupKey = ADULT_AGE_GROUPS.contains(r.ageGroup()) ? AKTIVE_LABEL : r.ageGroup();
      var period = r.date().minusDays(1).format(DTF);
      positions.computeIfAbsent(groupKey, k -> new LinkedHashMap<>()).put(period, r.rankingPosition());
      scores.putIfAbsent(period, r.score());
    }
    var positionsList =
        DIAGRAM_ORDER.stream()
            .filter(positions::containsKey)
            .map(ag -> new AgeGroupTimeSeries(ag, positions.get(ag)))
            .toList();
    return new DiagramDataView(positionsList, List.of(new ScoreTimeSeries("Punkte", scores)));
  }

  /// Derives which years and quarters have ranking data, from already-built
  /// [CompleteRankingRow]s.
  ///
  /// @param completeRankings rows produced by [#buildCompleteRankings(List)]
  /// @return quarters present per year, most recent year first; rows with an unparseable year
  ///     are skipped
  @SuppressWarnings("PMD.EmptyCatchBlock")
  public Map<Integer, Set<String>> buildAvailableData(List<CompleteRankingRow> completeRankings) {
    var result = new TreeMap<Integer, Set<String>>(Comparator.reverseOrder());
    for (var row : completeRankings) {
      var parts = row.date().split("/");
      if (parts.length != DATE_PARTS_SIZE) continue;
      try {
        var year = Integer.parseInt(parts[0]);
        result.computeIfAbsent(year, k -> new LinkedHashSet<>()).add(parts[1]);
      } catch (NumberFormatException _) {
        // skip rows with unparseable year
      }
    }
    return result;
  }

  @Nullable
  private static String computePositionChange(@Nullable Ranking prev, Ranking curr) {
    if (prev == null) return null;
    int diff = prev.rankingPosition() - curr.rankingPosition();
    return diff > 0 ? "+" + diff : String.valueOf(diff);
  }

  private static double parseScoreOrZero(Ranking ranking) {
    try {
      return Double.parseDouble(ranking.score().replace(',', '.'));
    } catch (NumberFormatException _) {
      return 0.0;
    }
  }

  @Nullable
  private static String computeScoreChange(@Nullable Ranking prev, Ranking curr) {
    if (prev == null) return null;
    if (SPECIAL_SCORES.contains(curr.score())) return null;
    try {
      double currScore = Double.parseDouble(curr.score().replace(',', '.'));
      double prevScore = parseScoreOrZero(prev);
      double diff = currScore - prevScore;
      if (diff == 0.0) return null;
      return formatScoreDiff(diff);
    } catch (NumberFormatException _) {
      return null;
    }
  }

  private static String formatScoreDiff(double diff) {
    long rounded10 = Math.round(diff * 10);
    String sign = rounded10 > 0 ? "+" : "";
    if (rounded10 % 10 == 0) {
      return sign + (rounded10 / 10);
    }
    return sign + String.format("%.1f", diff).replace('.', ',');
  }
}
