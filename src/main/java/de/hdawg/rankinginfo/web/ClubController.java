package de.hdawg.rankinginfo.web;

import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/clubs")
public class ClubController {

  private final ClubService clubService;

  public ClubController(ClubService clubService) {
    this.clubService = clubService;
  }

  @GetMapping
  public String index(
      @RequestParam(required = false) String commit,
      @RequestParam(required = false) String club,
      @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
      Model model) {

    var params = new HashMap<String, Object>();
    params.put("club", club);
    model.addAttribute("searchParams", params);

    if (commit != null && club != null && !club.isBlank()) {
      model.addAttribute("clubs", clubService.searchClubs(club));
    }
    return htmxRequest != null ? "clubs/results" : "clubs/index";
  }

  @GetMapping("/{id}")
  public String show(@PathVariable String id, Model model) {
    model.addAttribute("clubName", id);
    model.addAttribute("players", clubService.getClubDetail(id));
    return "clubs/show";
  }
}
