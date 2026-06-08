package de.hdawg.rankinginfo.web;

import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.hdawg.rankinginfo.repository.RankingRepository;
import de.hdawg.rankinginfo.service.PlayerService;

@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
@Controller
@RequestMapping("/players")
public class PlayerController {

  private static final String REDIRECT_PLAYER = "redirect:/players/";
  private static final String MODEL_PLAYERS = "players";

  private final PlayerService playerService;
  private final PlayerProfileService playerProfileService;
  private final RankingRepository rankingRepository;

  public PlayerController(
      PlayerService playerService,
      PlayerProfileService playerProfileService,
      RankingRepository rankingRepository) {
    this.playerService = playerService;
    this.playerProfileService = playerProfileService;
    this.rankingRepository = rankingRepository;
  }

  @GetMapping
  public String index(
      @RequestParam(required = false) String lastname,
      @RequestParam(required = false) String yob,
      @RequestParam(name = "dtb_id", required = false) String dtbId,
      @RequestParam(required = false) String commit,
      @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
      Model model,
      RedirectAttributes redirect,
      HttpServletResponse response) {

    if (dtbId != null && !dtbId.isBlank()) {
      var range = playerService.dtbIdRange(dtbId);
      var players = playerService.findPlayersByDtbIdRange(range[0], range[1]);
      if (players.size() == 1) return redirectToPlayer(players.get(0).dtbId(), htmxRequest, response);
      model.addAttribute(MODEL_PLAYERS, players);
    } else if (lastname != null && !lastname.isBlank() && yob != null && !yob.isBlank()) {
      int yobMale = playerService.yobToMaleMarker(yob);
      var players = playerService.findPlayersByLastnameAndYob(lastname.trim(), yobMale, yobMale + 100);
      if (players.size() == 1) return redirectToPlayer(players.get(0).dtbId(), htmxRequest, response);
      model.addAttribute(MODEL_PLAYERS, players);
    } else if (lastname != null && !lastname.isBlank()) {
      var players = playerService.findPlayersByLastname(lastname.trim());
      if (players.size() == 1) return redirectToPlayer(players.get(0).dtbId(), htmxRequest, response);
      model.addAttribute(MODEL_PLAYERS, players);
    } else if (yob != null && !yob.isBlank()) {
      int yobMale = playerService.yobToMaleMarker(yob);
      var players = playerService.findPlayersByYob(yobMale, yobMale + 100);
      if (players.size() == 1) return redirectToPlayer(players.get(0).dtbId(), htmxRequest, response);
      model.addAttribute(MODEL_PLAYERS, players);
    }

    var params = new java.util.HashMap<String, Object>();
    params.put("lastname", lastname);
    params.put("yob", yob);
    params.put("dtb_id", dtbId);
    model.addAttribute("params", params);
    return htmxRequest != null ? "players/index :: results" : "players/index";
  }

  private String redirectToPlayer(int dtbId, String htmxRequest, HttpServletResponse response) {
    if (htmxRequest != null) {
      response.setHeader("HX-Redirect", "/players/" + dtbId);
      return "players/index :: results";
    }
    return REDIRECT_PLAYER + dtbId;
  }

  @GetMapping("/{id}")
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

      var currentRankings =
          playerProfileService.buildCurrentRankings(rawRankings, currentQuarter, previousQuarter);
      var allTimeDiagram = playerProfileService.buildDiagramData(fullRankings);
      var recent12mDiagram =
          playerProfileService.buildDiagramData(
              fullRankings.stream().filter(r -> recent4Dates.contains(r.date())).toList());
      var completeRankings = playerProfileService.buildCompleteRankings(fullRankings);
      var availableData = playerProfileService.buildAvailableData(completeRankings);

      model.addAttribute("player", player);
      model.addAttribute("currentRankings", currentRankings);
      model.addAttribute("completeRankings", completeRankings);
      model.addAttribute("diagramData", allTimeDiagram.positions());
      model.addAttribute("diagramScoreData", allTimeDiagram.scores());
      model.addAttribute("recent12mData", recent12mDiagram.positions());
      model.addAttribute("recent12mScoreData", recent12mDiagram.scores());
      model.addAttribute("availableData", availableData);
      return "players/show";
    } catch (IndexOutOfBoundsException e) {
      redirect.addFlashAttribute("danger", "Spieler nicht gefunden");
      return "redirect:/players";
    }
  }
}
