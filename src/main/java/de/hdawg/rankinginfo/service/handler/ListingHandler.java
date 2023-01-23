package de.hdawg.rankinginfo.service.handler;

import de.hdawg.rankinginfo.service.actuator.ListingInfoContributor;
import de.hdawg.rankinginfo.service.exception.RankingPeriodException;
import de.hdawg.rankinginfo.service.services.ListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

/**
 * handler for dealing with listing requests.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ListingHandler {

  public static final String PATH_PARAM_QUARTER = "quarter";
  public static final String PATH_PARAM_AGE_GROUP = "ageGroup";
  public static final String PATH_PARAM_GENDER = "gender";

  private final ListingInfoContributor listingInfoContributor;
  private final ListingService listingService;

  /**
   * handler for retrieving an age group ranking list respecting search headers.
   *
   * @param serverRequest request received from the router
   * @return server response object containing the payload
   */
  public Mono<ServerResponse> retrieveAgeGroupListing(ServerRequest serverRequest) {
    log.debug("requesting ranking for quarter {} for age group {}",
        serverRequest.pathVariable(PATH_PARAM_QUARTER), serverRequest.pathVariable(PATH_PARAM_AGE_GROUP));
    // FIXME check modifier combination
    try {
      LocalDate rankingPeriod = checkAndMapRankingPeriod(serverRequest.pathVariable(PATH_PARAM_QUARTER));
      var rankings = listingService.getAgeGroupRankings(rankingPeriod, serverRequest.pathVariable(PATH_PARAM_AGE_GROUP),
          serverRequest.pathVariable(PATH_PARAM_GENDER), false, false, false);
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
    // FIXME check modifier combination
    try {
      LocalDate rankingPeriod = checkAndMapRankingPeriod(serverRequest.pathVariable(PATH_PARAM_QUARTER));
      var rankings = listingService.getAgeGroupRankingsFilteredByClub(rankingPeriod,
          serverRequest.pathVariable(PATH_PARAM_AGE_GROUP), serverRequest.pathVariable(PATH_PARAM_GENDER),
          false, false, false, "");
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

  boolean verifyCombinationOfModifiersIsValid(boolean yobRanking, boolean overallRanking, boolean endOfYearRanking) {
    if (yobRanking) {
      return (!overallRanking && !endOfYearRanking);
    } else {
      if (overallRanking) {
        return !endOfYearRanking;
      } else {
        return true;
      }
    }
  }
}
