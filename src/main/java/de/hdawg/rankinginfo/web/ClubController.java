package de.hdawg.rankinginfo.web;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.repository.RankingRepository;

@Controller
@RequestMapping("/clubs")
public class ClubController {

  private static final Map<String, String> ADULT_LABELS = Map.of("m00", "Herren", "w00", "Damen");

  private final RankingRepository rankingRepository;

  public ClubController(RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
  }

  @GetMapping
  public String index(
      @RequestParam(required = false) String commit,
      @RequestParam(required = false) String club,
      @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
      Model model) {

    var params = new HashMap<String, Object>();
    params.put("club", club);
    model.addAttribute("params", params);

    if (commit != null && club != null && !club.isBlank()) {
      var quarter = rankingRepository.findDistinctDatesDesc().stream().findFirst().orElse(null);
      if (quarter != null) {
        var youthByClub =
            rankingRepository
                .findByDateAndAgeGroupAndClubContaining(quarter, "overall", club)
                .stream()
                .collect(Collectors.groupingBy(Ranking::club));

        var allAdults = new ArrayList<Ranking>();
        allAdults.addAll(
            rankingRepository.findByDateAndAgeGroupAndClubContaining(quarter, "m00", club));
        allAdults.addAll(
            rankingRepository.findByDateAndAgeGroupAndClubContaining(quarter, "w00", club));
        var adultByClub = allAdults.stream().collect(Collectors.groupingBy(Ranking::club));

        var clubs =
            Stream.concat(youthByClub.keySet().stream(), adultByClub.keySet().stream())
                .distinct()
                .sorted()
                .map(
                    name ->
                        Map.of(
                            "name", name,
                            "youth_count", youthByClub.getOrDefault(name, List.of()).size(),
                            "adult_count", adultByClub.getOrDefault(name, List.of()).size()))
                .toList();
        model.addAttribute("clubs", clubs);
      }
    }
    return htmxRequest != null ? "clubs/index :: results" : "clubs/index";
  }

  @GetMapping("/{id}")
  public String show(@PathVariable String id, Model model) {
    var quarter = rankingRepository.findDistinctDatesDesc().stream().findFirst().orElse(null);
    var players = new LinkedHashMap<String, Object>();

    if (quarter != null) {
      var youth =
          rankingRepository
              .findByDateAndAgeGroupAndClubContaining(quarter, "overall", id)
              .stream()
              .filter(r -> r.club().equalsIgnoreCase(id))
              .toList();

      for (var entry : ADULT_LABELS.entrySet()) {
        var ag = entry.getKey();
        var label = entry.getValue();
        var adults =
            rankingRepository
                .findByDateAndAgeGroupAndClubContaining(quarter, ag, id)
                .stream()
                .filter(r -> r.club().equalsIgnoreCase(id))
                .toList();
        if (!adults.isEmpty()) {
          players.put(label, toPlayerMaps(adults, null));
        }
      }

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
                .collect(Collectors.groupingBy(r -> ageGroupRankings.get(r.dtbId()).ageGroup()));

        for (var ag : List.of("U12", "U14", "U16", "U18")) {
          var group = grouped.get(ag);
          if (group == null) continue;
          var sorted = group.stream().sorted(Comparator.comparingInt(Ranking::rankingPosition)).toList();
          players.put(ag, toPlayerMaps(sorted, ag));
        }
      }
    }

    model.addAttribute("clubName", id);
    model.addAttribute("players", players);
    return "clubs/show";
  }

  private static List<Map<String, Object>> toPlayerMaps(List<Ranking> rankings, String ageGroup) {
    return rankings.stream()
        .map(r -> buildPlayerMap(r, ageGroup))
        .toList();
  }

  private static Map<String, Object> buildPlayerMap(Ranking r, String ageGroup) {
    var map = new LinkedHashMap<String, Object>();
    map.put("dtb_id", r.dtbId());
    map.put("lastname", r.lastname());
    map.put("firstname", r.firstname());
    map.put("rank", r.rankingPosition());
    map.put("score", r.score());
    if (ageGroup != null) {
      map.put("age", ageGroup);
    }
    return map;
  }
}
