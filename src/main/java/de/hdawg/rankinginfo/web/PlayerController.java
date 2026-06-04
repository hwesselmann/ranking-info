package de.hdawg.rankinginfo.web;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.repository.RankingRepository;
import de.hdawg.rankinginfo.service.PlayerService;

@SuppressWarnings({"PMD.LooseCoupling", "PMD.AvoidLiteralsInIfCondition"})
@Controller
@RequestMapping("/players")
public class PlayerController {

  private static final Map<Integer, String> QUARTER_MAP =
      Map.of(1, "Q1", 4, "Q2", 7, "Q3", 10, "Q4");
  private static final List<String> ADULT_AGE_GROUPS = List.of("m00", "w00");
  private static final List<String> DIAGRAM_AGE_GROUPS =
      List.of("U12", "U14", "U16", "U18", "m00", "w00");
  private static final int DTB_ID_LENGTH = 8;
  private static final int DATE_PARTS_SIZE = 2;
  private static final String REDIRECT_PLAYER = "redirect:/players/";
  private static final String MODEL_PLAYERS = "players";
  private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  private final PlayerService playerService;
  private final RankingRepository rankingRepository;

  public PlayerController(PlayerService playerService, RankingRepository rankingRepository) {
    this.playerService = playerService;
    this.rankingRepository = rankingRepository;
  }

  @GetMapping
  public String index(
      @RequestParam(required = false) String lastname,
      @RequestParam(required = false) String yob,
      @RequestParam(name = "dtb_id", required = false) String dtbId,
      @RequestParam(required = false) String commit,
      Model model,
      RedirectAttributes redirect) {

    if (dtbId != null && !dtbId.isBlank()) {
      long idNum;
      try {
        idNum = Long.parseLong(dtbId.trim());
      } catch (NumberFormatException e) {
        idNum = 0L;
      }
      int missing = DTB_ID_LENGTH - Long.toString(idNum).length();
      int start = (int) (idNum * Math.pow(10.0, missing));
      int end = start + (int) Math.pow(10.0, missing) - 1;
      var players = playerService.findPlayersByDtbIdRange(start, end);
      if (players.size() == 1) return REDIRECT_PLAYER + players.get(0).dtbId();
      model.addAttribute(MODEL_PLAYERS, players);
    } else if (lastname != null && !lastname.isBlank() && yob != null && !yob.isBlank()) {
      int yobMale = Integer.parseInt(yob.trim().substring(2, 4)) + 100;
      var players = playerService.findPlayersByLastnameAndYob(lastname.trim(), yobMale, yobMale + 100);
      if (players.size() == 1) return REDIRECT_PLAYER + players.get(0).dtbId();
      model.addAttribute(MODEL_PLAYERS, players);
    } else if (lastname != null && !lastname.isBlank()) {
      var players = playerService.findPlayersByLastname(lastname.trim());
      if (players.size() == 1) return REDIRECT_PLAYER + players.get(0).dtbId();
      model.addAttribute(MODEL_PLAYERS, players);
    } else if (yob != null && !yob.isBlank()) {
      int yobMale = Integer.parseInt(yob.trim().substring(2, 4)) + 100;
      var players = playerService.findPlayersByYob(yobMale, yobMale + 100);
      if (players.size() == 1) return REDIRECT_PLAYER + players.get(0).dtbId();
      model.addAttribute(MODEL_PLAYERS, players);
    }

    var params = new java.util.HashMap<String, Object>();
    params.put("lastname", lastname);
    params.put("yob", yob);
    params.put("dtb_id", dtbId);
    model.addAttribute("params", params);
    return "players/index";
  }

  @GetMapping("/{id}")
  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  public String show(@PathVariable int id, Model model, RedirectAttributes redirect) {
    try {
      var player = playerService.loadPlayerProfile(id);

      var rawRankings =
          rankingRepository
              .findByDtbIdAndYobRankingFalseAndAgeGroupRankingFalseAndYearEndRankingFalseOrderByDateDescAgeGroupAsc(
                  id);
      var fullRankings =
          rankingRepository.findByDtbIdAndYearEndRankingFalseOrderByDateAscAgeGroupAsc(id);

      var allDates = rankingRepository.findDistinctDatesDesc();
      var currentQuarter = allDates.isEmpty() ? null : allDates.get(0);
      var previousQuarter = allDates.size() > 1 ? allDates.get(1) : null;
      var recent4Dates = Set.copyOf(allDates.stream().limit(4).toList());

      var currentRankings = buildCurrentRankings(rawRankings, currentQuarter, previousQuarter);
      var allTimeDiagram = buildDiagramData(fullRankings);
      var recent12mDiagram = buildDiagramData(
          fullRankings.stream().filter(r -> recent4Dates.contains(r.date())).toList());
      var completeRankings = buildCompleteRankings(fullRankings);
      var availableData = buildAvailableData(completeRankings);

      model.addAttribute("player", player);
      model.addAttribute("currentRankings", currentRankings);
      model.addAttribute("completeRankings", completeRankings);
      model.addAttribute("diagramData", allTimeDiagram.get("positions"));
      model.addAttribute("diagramScoreData", allTimeDiagram.get("scores"));
      model.addAttribute("recent12mData", recent12mDiagram.get("positions"));
      model.addAttribute("recent12mScoreData", recent12mDiagram.get("scores"));
      model.addAttribute("availableData", availableData);
      return "players/show";
    } catch (Exception e) {
      redirect.addFlashAttribute("danger", "Spieler nicht gefunden");
      return "redirect:/players";
    }
  }

  private static List<Map<String, Object>> buildCurrentRankings(
      List<Ranking> rawRankings,
      java.time.LocalDate currentQuarter,
      java.time.LocalDate previousQuarter) {
    if (currentQuarter == null) return List.of();
    return rawRankings.stream()
        .filter(r -> r.date().equals(currentQuarter) && !"overall".equals(r.ageGroup()))
        .map(r -> {
          var prev = previousQuarter == null ? null
              : rawRankings.stream()
                  .filter(p -> p.date().equals(previousQuarter) && p.ageGroup().equals(r.ageGroup()))
                  .findFirst().orElse(null);
          var entry = new LinkedHashMap<String, Object>();
          entry.put("age_group", r.ageGroup());
          entry.put("position", r.rankingPosition());
          entry.put("score", r.score());
          entry.put("position_change", computePositionChange(prev, r));
          entry.put("score_change", computeScoreChange(prev, r));
          return (Map<String, Object>) entry;
        })
        .sorted(
            Comparator.<Map<String, Object>, Integer>comparing(
                    m -> ADULT_AGE_GROUPS.contains(m.get("age_group")) ? 0 : 1)
                .thenComparing(m -> String.valueOf(m.get("age_group"))))
        .toList();
  }

  private static List<Map<String, Object>> buildCompleteRankings(List<Ranking> rankings) {
    var skipGroups = Set.of("overall");
    var byDate = rankings.stream()
        .filter(r -> !skipGroups.contains(r.ageGroup()))
        .collect(Collectors.groupingBy(
            Ranking::date,
            () -> new TreeMap<>(Comparator.reverseOrder()),
            Collectors.toList()));

    var result = new ArrayList<Map<String, Object>>();
    for (var entry : byDate.entrySet()) {
      var date = entry.getKey();
      var rows = entry.getValue();
      var quarter = QUARTER_MAP.get(date.getMonthValue());
      if (quarter == null) continue;
      var map = new LinkedHashMap<String, Object>();
      map.put("date", date.getYear() + "/" + quarter);
      map.put("score", rows.isEmpty() ? "" : rows.get(0).score());
      for (var r : rows) {
        var key = ADULT_AGE_GROUPS.contains(r.ageGroup()) ? "Aktive" : r.ageGroup();
        map.put(key, r.rankingPosition());
      }
      result.add(map);
    }
    return result;
  }

  private static Map<String, Object> buildDiagramData(List<Ranking> rankings) {
    var positions = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
    var scores = new LinkedHashMap<String, String>();
    for (var r : rankings) {
      if (!DIAGRAM_AGE_GROUPS.contains(r.ageGroup())) continue;
      var groupKey = ADULT_AGE_GROUPS.contains(r.ageGroup()) ? "Aktive" : r.ageGroup();
      var period = r.date().minusDays(1).format(DTF);
      positions.computeIfAbsent(groupKey, k -> new LinkedHashMap<>()).put(period, r.rankingPosition());
      scores.putIfAbsent(period, r.score());
    }
    var positionsList = List.of("U12", "U14", "U16", "U18", "Aktive").stream()
        .filter(positions::containsKey)
        .map(ag -> Map.of("name", ag, "data", (Object) positions.get(ag)))
        .toList();
    return Map.of(
        "positions", positionsList,
        "scores", List.of(Map.of("name", "Punkte", "data", (Object) scores)));
  }

  private static Map<Integer, Set<String>> buildAvailableData(List<Map<String, Object>> completeRankings) {
    var result = new TreeMap<Integer, Set<String>>(Comparator.reverseOrder());
    for (var row : completeRankings) {
      var dateStr = (String) row.get("date");
      if (dateStr == null) continue;
      var parts = dateStr.split("/");
      if (parts.length != DATE_PARTS_SIZE) continue;
      int year;
      try {
        year = Integer.parseInt(parts[0]);
      } catch (NumberFormatException e) {
        continue;
      }
      result.computeIfAbsent(year, k -> new java.util.LinkedHashSet<>()).add(parts[1]);
    }
    return result;
  }

  private static String computePositionChange(Ranking prev, Ranking curr) {
    if (prev == null) return null;
    int diff = prev.rankingPosition() - curr.rankingPosition();
    return diff > 0 ? "+" + diff : String.valueOf(diff);
  }

  private static String computeScoreChange(Ranking prev, Ranking curr) {
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
