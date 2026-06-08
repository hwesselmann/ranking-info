package de.hdawg.rankinginfo.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/federations")
public class FederationController {

  private final FederationService federationService;

  public FederationController(FederationService federationService) {
    this.federationService = federationService;
  }

  @GetMapping
  public String index(Model model) {
    model.addAttribute("federations", federationService.buildFederationData());
    return "federations/index";
  }
}
