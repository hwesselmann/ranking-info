package de.hdawg.rankinginfo.service.handler;

import de.hdawg.rankinginfo.service.actuator.ListingInfoContributor;
import de.hdawg.rankinginfo.service.exception.RankingPeriodException;
import de.hdawg.rankinginfo.service.services.ListingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * handler for dealing with listing requests.
 */
@Component
public class ListingHandler {

  public static final String PATH_PARAM_QUARTER = "quarter";
  public static final String PATH_PARAM_AGE_GROUP = "ageGroup";
  public static final String PATH_PARAM_GENDER = "gender";
  public static final String PATH_PARAM_CLUB = "club";
  public static final String PATH_PARAM_MODIFIER = "modifier";
  public static final String KEY_YOB = "yob";
  public static final String KEY_OVERALL = "overall";
  public static final String KEY_ENDOFYEAR = "endofyear";
  private static final Logger log = LoggerFactory.getLogger(ListingHandler.class);
  private final ListingInfoContributor listingInfoContributor;
  private final ListingService listingService;

  public ListingHandler(ListingInfoContributor listingInfoContributor, ListingService listingService) {
    this.listingInfoContributor = listingInfoContributor;
    this.listingService = listingService;
  }

  /**
   * handler for retrieving an age group ranking list respecting search headers.
   *
   * @param serverRequest request received from the router
   * @return server response object containing the payload
   */
  public Mono<ServerResponse> retrieveAgeGroupListing(ServerRequest serverRequest) {
    log.debug("requesting ranking for quarter {} for age group {}",
        serverRequest.pathVariable(PATH_PARAM_QUARTER), serverRequest.pathVariable(PATH_PARAM_AGE_GROUP));
    Map<String, Boolean> modifiers = mapModifier(serverRequest.pathVariable(PATH_PARAM_MODIFIER));
    try {
      LocalDate rankingPeriod = checkAndMapRankingPeriod(serverRequest.pathVariable(PATH_PARAM_QUARTER));
      var rankings = listingService.getAgeGroupRankings(rankingPeriod, serverRequest.pathVariable(PATH_PARAM_AGE_GROUP),
          serverRequest.pathVariable(PATH_PARAM_GENDER), modifiers.get(KEY_YOB), modifiers.get(KEY_OVERALL),
          modifiers.get(KEY_ENDOFYEAR));
      return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(rankings));
    } catch (RankingPeriodException rpe) {
      log.error("the quarter requested ({}) is invalid", serverRequest.pathVariable(PATH_PARAM_QUARTER));
      return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(rpe.getMessage()));
    }
  }

  /**
   * handler for retrieving an age group ranking list respecting search headers filtered by clubs.
   *
   * @param serverRequest request received from the router
   * @return server response object containing the payload
   */
  public Mono<ServerResponse> retrieveAgeGroupListingFilteredByClub(ServerRequest serverRequest) {
    log.debug("requesting ranking for quarter {} for age group {}",
        serverRequest.pathVariable(PATH_PARAM_QUARTER), serverRequest.pathVariable(PATH_PARAM_AGE_GROUP));
    Map<String, Boolean> modifiers = mapModifier(serverRequest.pathVariable(PATH_PARAM_MODIFIER));
    try {
      LocalDate rankingPeriod = checkAndMapRankingPeriod(serverRequest.pathVariable(PATH_PARAM_QUARTER));
      var rankings = listingService.getAgeGroupRankingsFilteredByClub(rankingPeriod,
          serverRequest.pathVariable(PATH_PARAM_AGE_GROUP), serverRequest.pathVariable(PATH_PARAM_GENDER),
          modifiers.get(KEY_YOB), modifiers.get(KEY_OVERALL), modifiers.get(KEY_ENDOFYEAR),
          serverRequest.pathVariable(PATH_PARAM_CLUB));
      return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(rankings));
    } catch (RankingPeriodException rpe) {
      log.error("the quarter requested ({}) is invalid", serverRequest.pathVariable(PATH_PARAM_QUARTER));
      return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(rpe.getMessage()));
    }
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
