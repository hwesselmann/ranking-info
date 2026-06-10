package de.hdawg.rankinginfo.web;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import de.hdawg.rankinginfo.domain.Ranking;
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
      @RequestParam(required = false) @Nullable String quarter,
      @RequestParam(required = false) @Nullable String gender,
      @RequestParam(name = "age_group", required = false) @Nullable String ageGroup,
      @RequestParam(name = "age_group_options", required = false) @Nullable String ageGroupOptions,
      @RequestParam(required = false) @Nullable String federation,
      @RequestParam(required = false) @Nullable String club,
      @RequestParam(name = "year_end", defaultValue = "0") String yearEnd,
      @RequestParam(required = false) @Nullable String commit,
      @RequestHeader(value = "HX-Request", defaultValue = "false") boolean htmxRequest,
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
    model.addAttribute("searchParams", params);

    if (commit != null && quarter != null && !quarter.isBlank()
        && gender != null && !gender.isBlank()) {
      var slug = RankingService.toAgeGroupSlug(gender, ageGroup);
      var filter = new RankingFilter(
          quarter,
          slug,
          ageGroupOptions != null && !ageGroupOptions.isBlank() ? ageGroupOptions : null,
          federation,
          club,
          "1".equals(yearEnd));
      var rankings = rankingService.findFilteredRankings(filter, 1, 1000);
      var dtbIds = rankings.getContent().stream().map(Ranking::dtbId).toList();
      var prevPositions = rankingService.findPreviousPositions(filter, dtbIds);
      model.addAttribute("rankings", toRows(rankings.getContent(), prevPositions));
    }
    return htmxRequest ? "listing/results" : "listing/index";
  }

  private static List<RankingRow> toRows(
      List<Ranking> rankings, java.util.Map<Integer, Integer> prevPositions) {
    var rows = new ArrayList<RankingRow>(rankings.size());
    int rowNumber = 1;
    for (var r : rankings) {
      var prev = prevPositions.get(r.dtbId());
      String positionChange = null;
      if (prev != null) {
        if (prev > r.rankingPosition()) positionChange = "up";
        else if (prev < r.rankingPosition()) positionChange = "down";
      }
      rows.add(new RankingRow(
          rowNumber,
          r.rankingPosition(),
          positionChange,
          r.lastname() + ", " + r.firstname(),
          String.valueOf(r.dtbId()),
          "/players/" + r.dtbId(),
          "/images/" + r.nationality().toLowerCase(Locale.ROOT) + ".svg",
          r.nationality(),
          r.club(),
          "/clubs/" + UriUtils.encodePathSegment(r.club(), StandardCharsets.UTF_8),
          r.federation(),
          r.score()));
      rowNumber++;
    }
    return rows;
  }

}
