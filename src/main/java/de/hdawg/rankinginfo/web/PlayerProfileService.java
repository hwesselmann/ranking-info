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
import de.hdawg.rankinginfo.web.viewmodel.AgeGroupTimeSeries;
import de.hdawg.rankinginfo.web.viewmodel.CompleteRankingRow;
import de.hdawg.rankinginfo.web.viewmodel.CurrentRankingRow;
import de.hdawg.rankinginfo.web.viewmodel.DiagramDataView;
import de.hdawg.rankinginfo.web.viewmodel.ScoreTimeSeries;

@SuppressWarnings({"PMD.LooseCoupling", "PMD.AvoidLiteralsInIfCondition"})
@Service
public class PlayerProfileService {

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

  private static final Map<Integer, String> QUARTER_MAP =
      Map.of(1, "Q1", 4, "Q2", 7, "Q3", 10, "Q4");
  private static final List<String> ADULT_AGE_GROUPS = List.of("m00", "w00");
  private static final List<String> DIAGRAM_AGE_GROUPS =
      List.of("U12", "U14", "U16", "U18", "m00", "w00");
  private static final List<String> DIAGRAM_ORDER = List.of("U12", "U14", "U16", "U18", "Aktive");
  private static final String AGE_GROUP_OVERALL = "overall";
  private static final String AKTIVE_LABEL = "Aktive";
  private static final int DATE_PARTS_SIZE = 2;
  private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  @Transactional(readOnly = true)
  public Optional<PlayerProfile> loadProfile(int dtbId) {
    var repo = rankingRepository;
    if (repo == null) throw new IllegalStateException("RankingRepository not injected");

    var rawRankings =
        repo.findByDtbIdAndYobRankingFalseAndAgeGroupRankingFalseAndYearEndRankingFalseOrderByDateDescAgeGroupAsc(
            dtbId);
    if (rawRankings.isEmpty()) return Optional.empty();

    var current = rawRankings.getFirst();
    var clubs =
        rawRankings.stream()
            .map(r -> new Club(r.club(), r.federation()))
            .collect(Collectors.toMap(Club::name, c -> c, (a, b) -> a))
            .values()
            .stream()
            .sorted(Comparator.comparing(Club::name))
            .toList();
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

    var currentRankings = buildCurrentRankings(rawRankings, currentQuarter, previousQuarter);
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

  public List<CurrentRankingRow> buildCurrentRankings(
      List<Ranking> rawRankings,
      @Nullable LocalDate currentQuarter,
      @Nullable LocalDate previousQuarter) {
    if (currentQuarter == null) return List.of();
    return rawRankings.stream()
        .filter(r -> r.date().equals(currentQuarter) && !AGE_GROUP_OVERALL.equals(r.ageGroup()))
        .map(
            r -> {
              var prev =
                  previousQuarter == null
                      ? null
                      : rawRankings.stream()
                          .filter(
                              p ->
                                  p.date().equals(previousQuarter)
                                      && p.ageGroup().equals(r.ageGroup()))
                          .findFirst()
                          .orElse(null);
              return new CurrentRankingRow(
                  r.ageGroup(),
                  r.rankingPosition(),
                  r.score(),
                  computePositionChange(prev, r),
                  computeScoreChange(prev, r));
            })
        .sorted(
            Comparator.<CurrentRankingRow, Integer>comparing(
                    row -> ADULT_AGE_GROUPS.contains(row.ageGroup()) ? 0 : 1)
                .thenComparing(CurrentRankingRow::ageGroup))
        .toList();
  }

  public List<CompleteRankingRow> buildCompleteRankings(List<Ranking> rankings) {
    var byDate =
        rankings.stream()
            .filter(r -> !AGE_GROUP_OVERALL.equals(r.ageGroup()))
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

  public DiagramDataView buildDiagramData(List<Ranking> rankings) {
    var positions = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
    var scores = new LinkedHashMap<String, String>();
    for (var r : rankings.stream().sorted(Comparator.comparing(Ranking::date)).toList()) {
      if (!DIAGRAM_AGE_GROUPS.contains(r.ageGroup())) continue;
      var groupKey = ADULT_AGE_GROUPS.contains(r.ageGroup()) ? AKTIVE_LABEL : r.ageGroup();
      var period = r.date().minusDays(1).format(DTF);
      positions
          .computeIfAbsent(groupKey, k -> new LinkedHashMap<>())
          .put(period, r.rankingPosition());
      scores.putIfAbsent(period, r.score());
    }
    var positionsList =
        DIAGRAM_ORDER.stream()
            .filter(positions::containsKey)
            .map(ag -> new AgeGroupTimeSeries(ag, positions.get(ag)))
            .toList();
    return new DiagramDataView(positionsList, List.of(new ScoreTimeSeries("Punkte", scores)));
  }

  @SuppressWarnings("PMD.EmptyCatchBlock")
  public Map<Integer, Set<String>> buildAvailableData(List<CompleteRankingRow> completeRankings) {
    var result = new TreeMap<Integer, Set<String>>(Comparator.reverseOrder());
    for (var row : completeRankings) {
      var parts = row.date().split("/");
      if (parts.length != DATE_PARTS_SIZE) continue;
      try {
        var year = Integer.parseInt(parts[0]);
        result.computeIfAbsent(year, k -> new LinkedHashSet<>()).add(parts[1]);
      } catch (NumberFormatException e) {
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

  @Nullable
  private static String computeScoreChange(@Nullable Ranking prev, Ranking curr) {
    if (prev == null) return null;
    try {
      double currScore = Double.parseDouble(curr.score().replace(',', '.'));
      double prevScore;
      try {
        prevScore = Double.parseDouble(prev.score().replace(',', '.'));
      } catch (NumberFormatException e) {
        prevScore = 0.0;
      }
      double diff = currScore - prevScore;
      if (diff == 0.0) return null;
      return diff > 0 ? "+" + diff : String.valueOf(diff);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
