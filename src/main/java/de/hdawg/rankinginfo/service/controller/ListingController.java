package de.hdawg.rankinginfo.service.controller;

import de.hdawg.rankinginfo.service.actuator.ListingInfoContributor;
import de.hdawg.rankinginfo.service.exception.RankingPeriodException;
import de.hdawg.rankinginfo.service.model.listing.Listing;
import de.hdawg.rankinginfo.service.services.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the listing endpoint.
 */
@RestController
public class ListingController {

  public static final String KEY_YOB = "yob";
  public static final String KEY_OVERALL = "overall";
  public static final String KEY_ENDOFYEAR = "endofyear";
  private static final Logger log = LoggerFactory.getLogger(ListingController.class);
  private final ListingInfoContributor listingInfoContributor;
  private final ListingService listingService;

  public ListingController(ListingInfoContributor listingInfoContributor, ListingService listingService) {
    this.listingInfoContributor = listingInfoContributor;
    this.listingService = listingService;
  }

  /**
   * get listings for specified quarter, gender, age group and modifiers.
   *
   * @param quarter  ranking period in format yyyy-mm-dd
   * @param gender   gender to request listing for: 'boys' or 'girls
   * @param ageGroup requested age group: 'u11','u12','u13','u14','u15','u16','u17','u18'
   * @param modifier get different data views. valid: 'official', 'yob', 'overall', 'endofyear'
   * @return listing container with requested rankings
   */
  @Operation(summary = "get listings for specified quarter, gender, age group and modifiers")
  @GetMapping(path = "/listing/{quarter}/{gender}/{ageGroup}/{modifier}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Listing> getRequestedListing(
      @Parameter(description = "ranking period in format yyyy-mm-dd")
      @PathVariable(value = "quarter") String quarter,
      @Parameter(description = "gender to request listing for: 'boys' or 'girls'")
      @PathVariable(value = "gender") String gender,
      @Parameter(description = "requested age group: 'u11','u12','u13','u14','u15','u16','u17','u18'")
      @PathVariable(value = "ageGroup") String ageGroup,
      @Parameter(description = "get different data views. valid: 'official', 'yob', 'overall', 'endofyear'")
      @PathVariable(value = "modifier") String modifier) {

    log.debug("requesting ranking for quarter {} for age group {}", quarter, ageGroup);
    Map<String, Boolean> modifiers = mapModifier(modifier);
    LocalDate rankingPeriod = checkAndMapRankingPeriod(quarter);
    return Mono.fromCallable(() -> listingService.getAgeGroupRankings(rankingPeriod, ageGroup, gender, modifiers.get(KEY_YOB),
        modifiers.get(KEY_OVERALL), modifiers.get(KEY_ENDOFYEAR)));
  }

  /**
   * get listings for specified quarter, gender, age group and modifiers filtered by club string.
   *
   * @param quarter  ranking period in format yyyy-mm-dd
   * @param gender   gender to request listing for: 'boys' or 'girls
   * @param ageGroup requested age group: 'u11','u12','u13','u14','u15','u16','u17','u18'
   * @param modifier get different data views. valid: 'official', 'yob', 'overall', 'endofyear'
   * @param club     club name or name part to filter for
   * @return listing container with requested rankings
   */
  @Operation(summary = "get listings for specified quarter, gender, age group and modifiers filtered by club string")
  @GetMapping(path = "/listing/{quarter}/{gender}/{ageGroup}/{modifier}/{club}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Listing> getRequestedListingWithClubFilter(
      @Parameter(description = "ranking period in format yyyy-mm-dd")
      @PathVariable(value = "quarter") String quarter,
      @Parameter(description = "gender to request listing for: 'boys' or 'girls'")
      @PathVariable(value = "gender") String gender,
      @Parameter(description = "requested age group: 'u11','u12','u13','u14','u15','u16','u17','u18'")
      @PathVariable(value = "ageGroup") String ageGroup,
      @Parameter(description = "get different data views. valid: 'official', 'yob', 'overall', 'endofyear'")
      @PathVariable(value = "modifier") String modifier,
      @Parameter(description = "club name or name part to filter for")
      @PathVariable(value = "club") String club) {

    log.debug("requesting ranking for quarter {} for age group {}", quarter, ageGroup);
    Map<String, Boolean> modifiers = mapModifier(modifier);
    LocalDate rankingPeriod = checkAndMapRankingPeriod(quarter);
    return Mono.fromCallable(() -> listingService.getAgeGroupRankingsFilteredByClub(rankingPeriod, ageGroup, gender,
        modifiers.get(KEY_YOB), modifiers.get(KEY_OVERALL), modifiers.get(KEY_ENDOFYEAR), club));
  }

  LocalDate checkAndMapRankingPeriod(String input) throws RankingPeriodException {
    String[] dateParts = input.split("-");
    int year = Integer.parseInt(dateParts[0]);
    if (year < 2019 || year > 9999) {
      throw new RankingPeriodException("the year for the requested ranking period is not valid");
    }
    int month = Integer.parseInt(dateParts[1]);
    if (!List.of(3, 6, 9, 12).contains(month)) {
      throw new RankingPeriodException("requested period is not a valid ranking period");
    }
    int day = Integer.parseInt(dateParts[2]);
    if (month == 3 || month == 12) {
      if (day != 31) {
        throw new RankingPeriodException("the requested ranking period start with an invalid day");
      }
    } else {
      if (day != 30) {
        throw new RankingPeriodException("the requested ranking period start with an invalid day");
      }
    }
    return LocalDate.of(year, month, day);
  }

  Map<String, Boolean> mapModifier(String pathVariable) {
    Map<String, Boolean> modifiers = new HashMap<>();
    modifiers.put(KEY_YOB, false);
    modifiers.put(KEY_OVERALL, false);
    modifiers.put(KEY_ENDOFYEAR, false);
    if ("official".equalsIgnoreCase(pathVariable)) {
      return modifiers;
    }
    if (!pathVariable.isEmpty() && !pathVariable.isBlank()) {
      switch (pathVariable) {
        case KEY_YOB -> modifiers.put(KEY_YOB, true);
        case KEY_OVERALL -> modifiers.put(KEY_OVERALL, true);
        case KEY_ENDOFYEAR -> modifiers.put(KEY_ENDOFYEAR, true);
        default -> throw new IllegalStateException("Unexpected value for modifier: " + pathVariable);
      }
    }
    return modifiers;
  }
}
