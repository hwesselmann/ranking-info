package de.hdawg.rankinginfo.web;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import de.hdawg.rankinginfo.service.PlayerService;

@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
@Controller
@RequestMapping("/players")
public class PlayerController {

  private static final String REDIRECT_PLAYER = "redirect:/players/";
  private static final String MODEL_PLAYERS = "players";

  private final PlayerService playerService;
  private final PlayerProfileService playerProfileService;
  private final ObjectMapper objectMapper;

  public PlayerController(
      PlayerService playerService,
      PlayerProfileService playerProfileService,
      ObjectMapper objectMapper) {
    this.playerService = playerService;
    this.playerProfileService = playerProfileService;
    this.objectMapper = objectMapper;
  }

  @GetMapping
  public String index(
      @RequestParam(required = false) String lastname,
      @RequestParam(required = false) String yob,
      @RequestParam(name = "dtb_id", required = false) String dtbId,
      @RequestParam(required = false) String commit,
      @RequestHeader(value = "HX-Request", required = false, defaultValue = "false") boolean isHtmxRequest,
      Model model,
      RedirectAttributes redirect,
      HttpServletResponse response) {

    if (dtbId != null && !dtbId.isBlank()) {
      var range = playerService.dtbIdRange(dtbId);
      var players = playerService.findPlayersByDtbIdRange(range[0], range[1]);
      if (players.size() == 1) {
        return redirectToPlayer(players.getFirst().dtbId(), isHtmxRequest, response);
      }
      model.addAttribute(MODEL_PLAYERS, players);
    } else if (lastname != null && !lastname.isBlank() && yob != null && !yob.isBlank()) {
      int yobMale = playerService.yobToMaleMarker(yob);
      var players = playerService.findPlayersByLastnameAndYob(lastname.trim(), yobMale,
          yobMale + 100);
      if (players.size() == 1) {
        return redirectToPlayer(players.getFirst().dtbId(), isHtmxRequest, response);
      }
      model.addAttribute(MODEL_PLAYERS, players);
    } else if (lastname != null && !lastname.isBlank()) {
      var players = playerService.findPlayersByLastname(lastname.trim());
      if (players.size() == 1) {
        return redirectToPlayer(players.getFirst().dtbId(), isHtmxRequest, response);
      }
      model.addAttribute(MODEL_PLAYERS, players);
    } else if (yob != null && !yob.isBlank()) {
      int yobMale = playerService.yobToMaleMarker(yob);
      var players = playerService.findPlayersByYob(yobMale, yobMale + 100);
      if (players.size() == 1) {
        return redirectToPlayer(players.getFirst().dtbId(), isHtmxRequest, response);
      }
      model.addAttribute(MODEL_PLAYERS, players);
    }

    var params = new java.util.HashMap<String, Object>();
    params.put("lastname", lastname);
    params.put("yob", yob);
    params.put("dtb_id", dtbId);
    model.addAttribute("searchParams", params);
    return isHtmxRequest ? "players/results" : "players/index";
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JacksonException _) {
      return "[]";
    }
  }

  private String redirectToPlayer(int dtbId, boolean isHtmxRequest, HttpServletResponse response) {
    if (isHtmxRequest) {
      response.setHeader("HX-Redirect", "/players/" + dtbId);
      return "players/results";
    }
    return REDIRECT_PLAYER + dtbId;
  }

  @GetMapping("/{id}")
  public String show(@PathVariable int id, Model model, RedirectAttributes redirect) {
    var profileOpt = playerProfileService.loadProfile(id);
    if (profileOpt.isEmpty()) {
      redirect.addFlashAttribute("danger", "Spieler nicht gefunden");
      return "redirect:/players";
    }
    var p = profileOpt.get();
    model.addAttribute("player", p.player());
    model.addAttribute("currentRankings", p.currentRankings());
    model.addAttribute("completeRankings", p.completeRankings());
    model.addAttribute("diagramDataJson", toJson(p.allTimeDiagram().positions()));
    model.addAttribute("diagramScoreDataJson", toJson(p.allTimeDiagram().scores()));
    model.addAttribute("recent12mDataJson", toJson(p.recent12mDiagram().positions()));
    model.addAttribute("recent12mScoreDataJson", toJson(p.recent12mDiagram().scores()));
    model.addAttribute("availableData", p.availableData());
    return "players/show";
  }
}
