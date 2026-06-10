package de.hdawg.rankinginfo.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingRepository;

@Controller
public class StaticPageController {

  private static final int MALE_DTB_ID_START = 10_000_000;
  private static final int MALE_DTB_ID_END = 19_999_999;
  private static final int FEMALE_DTB_ID_START = 20_000_000;
  private static final int FEMALE_DTB_ID_END = 29_999_999;

  private static final int YEAR_END_MONTH = 12;
  private static final int YEAR_END_DAY = 31;

  private final RankingRepository rankingRepository;
  private final ImportHistoryRepository importHistoryRepository;

  private final String domain;
  private final String imprintName;
  private final String imprintStreet;
  private final String imprintZipcode;
  private final String imprintCity;
  private final String imprintPhone;
  private final String imprintMail;

  private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  public StaticPageController(
      RankingRepository rankingRepository,
      ImportHistoryRepository importHistoryRepository,
      @Value("${imprint.domain:localhost}") String domain,
      @Value("${imprint.name:}") String imprintName,
      @Value("${imprint.street:}") String imprintStreet,
      @Value("${imprint.zipcode:}") String imprintZipcode,
      @Value("${imprint.city:}") String imprintCity,
      @Value("${imprint.phone:}") String imprintPhone,
      @Value("${imprint.mail:}") String imprintMail) {
    this.rankingRepository = rankingRepository;
    this.importHistoryRepository = importHistoryRepository;
    this.domain = domain;
    this.imprintName = imprintName;
    this.imprintStreet = imprintStreet;
    this.imprintZipcode = imprintZipcode;
    this.imprintCity = imprintCity;
    this.imprintPhone = imprintPhone;
    this.imprintMail = imprintMail;
  }

  @GetMapping("/")
  public String home(Model model) {
    var firstDate = rankingRepository.findDistinctDatesDesc().stream().reduce((a, b) -> b).orElse(null);
    model.addAttribute("start", firstDate != null ? firstDate.minusDays(1).format(dtf) : "xx.xx.xxxx");
    return "static_pages/home";
  }

  @GetMapping("/help")
  public String help(Model model) {
    return "static_pages/help";
  }

  @GetMapping("/about")
  public String about(Model model) {
    addImprintData(model);
    return "static_pages/about";
  }

  @GetMapping("/privacy")
  public String privacy(Model model) {
    addImprintData(model);
    return "static_pages/privacy";
  }

  @GetMapping("/status")
  public String status(Model model) {
    model.addAttribute(
        "playerMaleCount",
        rankingRepository.countDistinctDtbIdInRange(MALE_DTB_ID_START, MALE_DTB_ID_END));
    model.addAttribute(
        "playerFemaleCount",
        rankingRepository.countDistinctDtbIdInRange(FEMALE_DTB_ID_START, FEMALE_DTB_ID_END));
    model.addAttribute("rankingCount", rankingRepository.count());

    var lastUpdated = importHistoryRepository.findMaxImportedAt();
    model.addAttribute(
        "dateLastUpdated",
        lastUpdated != null
            ? lastUpdated.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            : "");

    var quartersByYear =
        rankingRepository.findDistinctDatesDesc().stream()
            .filter(d -> !(d.getMonthValue() == YEAR_END_MONTH && d.getDayOfMonth() == YEAR_END_DAY))
            .collect(
                Collectors.groupingBy(
                    LocalDate::getYear,
                    () -> new TreeMap<>(Comparator.reverseOrder()),
                    Collectors.mapping(LocalDate::getMonthValue, Collectors.toUnmodifiableSet())));
    model.addAttribute("quartersByYear", quartersByYear);
    return "static_pages/status";
  }

  private void addImprintData(Model model) {
    model.addAttribute("imprintDomain", domain);
    model.addAttribute("imprintName", imprintName);
    model.addAttribute("imprintStreet", imprintStreet);
    model.addAttribute("imprintZipcode", imprintZipcode);
    model.addAttribute("imprintCity", imprintCity);
    model.addAttribute("imprintPhone", imprintPhone);
    model.addAttribute("imprintMail", imprintMail);
  }
}
