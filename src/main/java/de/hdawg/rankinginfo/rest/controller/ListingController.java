package de.hdawg.rankinginfo.rest.controller;

import de.hdawg.rankinginfo.model.AgeGroup;
import de.hdawg.rankinginfo.model.Gender;
import de.hdawg.rankinginfo.rest.exception.RankingPeriodException;
import de.hdawg.rankinginfo.rest.model.listing.Listing;
import de.hdawg.rankinginfo.rest.model.listing.ListingModifier;
import de.hdawg.rankinginfo.rest.services.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the listing endpoint.
 */
@RestController
public class ListingController {

  private static final Logger log = LoggerFactory.getLogger(ListingController.class);
  private final ListingService listingService;

  public ListingController(ListingService listingService) {
    this.listingService = listingService;
  }

  /**
   * get listings for specified quarter, gender, age group and modifiers.
   *
   * @param quarter  ranking period in format yyyy-mm-dd
   * @param gender   gender to request listing for: 'boys' or 'girls'
   * @param ageGroup requested age group: 'u11','u12','u13','u14','u15','u16','u17','u18'
   * @param modifier get different data views. valid: 'official', 'yob', 'overall', 'end_of_year'
   * @return listing container with requested rankings
   */
  @Operation(summary = "get listings for specified quarter, gender, age group and modifiers", tags = "listing")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "successfully received the requested listing.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = Listing.class))}),
      @ApiResponse(responseCode = "400", description = "unknown ranking period or parameters passed for request", content = @Content)
  })
  @GetMapping(path = "/listings/{quarter}/{gender}/{ageGroup}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Listing> getRequestedListing(
      @Parameter(in = ParameterIn.PATH, example = "yyyy-mm-dd", description = "ranking period")
      @PathVariable(value = "quarter") String quarter,
      @Parameter(in = ParameterIn.PATH, description = "gender to request listing for")
      @PathVariable(value = "gender") Gender gender,
      @Parameter(in = ParameterIn.PATH, description = "requested age group")
      @PathVariable(value = "ageGroup") AgeGroup ageGroup,
      @Parameter(in = ParameterIn.QUERY, description = "get different data views.")
      @RequestParam(value = "modifier", required = false) ListingModifier modifier) {

    log.debug("requesting ranking for quarter {} for age group {}", quarter, ageGroup);
    Map<ListingModifier, Boolean> modifiers = mapModifier(modifier);
    LocalDate rankingPeriod = checkAndMapRankingPeriod(quarter);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(listingService.getAgeGroupRankings(rankingPeriod, ageGroup, gender,
            modifiers.get(ListingModifier.yob), modifiers.get(ListingModifier.overall),
            modifiers.get(ListingModifier.end_of_year)));
  }

  /**
   * get listings for specified quarter, gender, age group and modifiers filtered by club string.
   *
   * @param quarter  ranking period in format yyyy-mm-dd
   * @param gender   gender to request listing for: 'boys' or 'girls'
   * @param ageGroup requested age group: 'u11','u12','u13','u14','u15','u16','u17','u18'
   * @param modifier get different data views. valid: 'official', 'yob', 'overall', 'end_of_year'
   * @param club     club name or name part to filter for
   * @return listing container with requested rankings
   */
  @Operation(summary = "get listings for specified quarter, gender, age group and modifiers filtered by club string", tags = "listing")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "successfully received the requested listing.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = Listing.class))}),
      @ApiResponse(responseCode = "400", description = "unknown ranking period or parameters passed for request", content = @Content)
  })
  @GetMapping(path = "/listings/{quarter}/{gender}/{ageGroup}/{club}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Listing> getRequestedListingWithClubFilter(
      @Parameter(in = ParameterIn.PATH, example = "yyyy-mm-dd", description = "ranking period")
      @PathVariable(value = "quarter") String quarter,
      @Parameter(description = "gender to request listing for")
      @PathVariable(value = "gender") Gender gender,
      @Parameter(in = ParameterIn.PATH, description = "requested age group")
      @PathVariable(value = "ageGroup") AgeGroup ageGroup,
      @Parameter(in = ParameterIn.PATH, description = "club name or name part to filter for")
      @PathVariable(value = "club") String club,
      @Parameter(in = ParameterIn.QUERY, description = "get different data views.")
      @RequestParam(value = "modifier", required = false) ListingModifier modifier) {

    log.debug("requesting ranking for quarter {} for age group {}", quarter, ageGroup);
    Map<ListingModifier, Boolean> modifiers = mapModifier(modifier);
    LocalDate rankingPeriod = checkAndMapRankingPeriod(quarter);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(listingService.getAgeGroupRankingsFilteredByClub(rankingPeriod, ageGroup,
            gender,
            modifiers.get(ListingModifier.yob), modifiers.get(ListingModifier.overall),
            modifiers.get(ListingModifier.end_of_year),
            club));
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

  Map<ListingModifier, Boolean> mapModifier(ListingModifier modifier) {
    Map<ListingModifier, Boolean> modifiers = new EnumMap<>(ListingModifier.class);
    modifiers.put(ListingModifier.yob, false);
    modifiers.put(ListingModifier.overall, false);
    modifiers.put(ListingModifier.end_of_year, false);
    if (modifier == null || ListingModifier.official.equals(modifier)) {
      return modifiers;
    }
    switch (modifier) {
      case yob -> modifiers.put(ListingModifier.yob, true);
      case overall -> modifiers.put(ListingModifier.overall, true);
      case end_of_year -> modifiers.put(ListingModifier.end_of_year, true);
      default -> log.trace("check this condition as it should not be reachable");
    }
    return modifiers;
  }
}
