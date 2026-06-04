package de.hdawg.rankinginfo.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import de.hdawg.rankinginfo.repository.RankingRepository;

@Controller
@RequestMapping("/federations")
public class FederationController {

  private static final Map<String, String> FEDERATION_NAMES =
      Map.ofEntries(
          Map.entry("BAD", "Baden"),
          Map.entry("BB", "Berlin-Brandenburg"),
          Map.entry("BTV", "Bayern"),
          Map.entry("HAM", "Hamburg"),
          Map.entry("HTV", "Hessen"),
          Map.entry("RPF", "Rheinland-Pfalz"),
          Map.entry("SLH", "Schleswig-Holstein"),
          Map.entry("STB", "Saarland"),
          Map.entry("STV", "Sachsen"),
          Map.entry("TMV", "Mecklenburg-Vorpommern"),
          Map.entry("TNB", "Niedersachsen-Bremen"),
          Map.entry("TSA", "Sachsen-Anhalt"),
          Map.entry("TTV", "Thüringen"),
          Map.entry("TVM", "Mittelrhein"),
          Map.entry("TVN", "Niederrhein"),
          Map.entry("WTB", "Württemberg"),
          Map.entry("WTV", "Westfalen"));

  private final RankingRepository rankingRepository;

  public FederationController(RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
  }

  @GetMapping
  public String index(Model model) {
    var quarter = rankingRepository.findDistinctDatesDesc().stream().findFirst().orElse(null);
    var federations = new LinkedHashMap<String, Map<String, Integer>>();

    if (quarter != null) {
      for (var gender : new String[] {"m", "w"}) {
        int dtbIdStart = "m".equals(gender) ? 10_000_000 : 20_000_000;
        for (Object[] row :
            rankingRepository.countYouthByFederationAndAgeGroup(
                quarter, dtbIdStart, dtbIdStart + 9_999_999)) {
          var fed = FEDERATION_NAMES.getOrDefault((String) row[0], (String) row[0]);
          var ag = (String) row[1];
          var count = ((Long) row[2]).intValue();
          federations.computeIfAbsent(fed, k -> new LinkedHashMap<>()).put(ag + gender, count);
        }
      }
      for (var ag : new String[] {"m00", "w00"}) {
        for (Object[] row : rankingRepository.countAdultByFederation(quarter, ag)) {
          var fed = FEDERATION_NAMES.getOrDefault((String) row[0], (String) row[0]);
          var count = ((Long) row[1]).intValue();
          if (federations.containsKey(fed)) {
            federations.get(fed).put(ag, count);
          }
        }
      }
    }

    model.addAttribute("federations", federations);
    return "federations/index";
  }
}
