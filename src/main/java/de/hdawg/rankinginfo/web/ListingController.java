package de.hdawg.rankinginfo.web;

import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.hdawg.rankinginfo.service.RankingFilter;
import de.hdawg.rankinginfo.service.RankingService;

@Controller
@RequestMapping("/listings")
public class ListingController {

  private final RankingService rankingService;

  public ListingController(RankingService rankingService) {
    this.rankingService = rankingService;
  }

  @GetMapping
  public String index(
      @RequestParam(required = false) String quarter,
      @RequestParam(required = false) String gender,
      @RequestParam(name = "age_group", required = false) String ageGroup,
      @RequestParam(name = "age_group_options", required = false) String ageGroupOptions,
      @RequestParam(required = false) String federation,
      @RequestParam(required = false) String club,
      @RequestParam(name = "year_end", defaultValue = "0") String yearEnd,
      @RequestParam(required = false) String commit,
      Model model) {

    model.addAttribute("quarters", rankingService.fetchAvailableQuarters());
    model.addAttribute("federations", rankingService.fetchFederations());

    var params = new HashMap<String, Object>();
    params.put("quarter", quarter);
    params.put("gender", gender);
    params.put("age_group", ageGroup);
    params.put("age_group_options", ageGroupOptions);
    params.put("federation", federation);
    params.put("club", club);
    params.put("year_end", yearEnd);
    model.addAttribute("params", params);

    if (commit != null && quarter != null && !quarter.isBlank()
        && gender != null && !gender.isBlank()) {
      var slug = toAgeGroupSlug(gender, ageGroup);
      var filter = new RankingFilter(
          quarter,
          slug,
          ageGroupOptions != null && !ageGroupOptions.isBlank() ? ageGroupOptions : null,
          federation,
          club,
          "1".equals(yearEnd));
      var rankings = rankingService.findFilteredRankings(filter, 1, 1000);
      var dtbIds = rankings.getContent().stream().map(r -> r.dtbId()).toList();
      var prevPositions = rankingService.findPreviousPositions(filter, dtbIds);
      model.addAttribute("rankings", rankings.getContent());
      model.addAttribute("previousPositions", prevPositions);
    }
    return "listing/index";
  }

  private static String toAgeGroupSlug(String gender, String ageGroup) {
    return switch (gender) {
      case "Herren" -> "m00";
      case "Damen" -> "w00";
      case "Junioren" ->
          (ageGroup == null || ageGroup.isBlank()) ? "overall" : "m" + ageGroup.toLowerCase();
      case "Juniorinnen" ->
          (ageGroup == null || ageGroup.isBlank()) ? "overall" : "w" + ageGroup.toLowerCase();
      default -> "overall";
    };
  }
}
