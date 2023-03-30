package de.hdawg.rankinginfo.service.controller;

import de.hdawg.rankinginfo.service.model.club.ClubPlayer;
import de.hdawg.rankinginfo.service.model.club.ClubSearchResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Endpoints for club data.
 */
@RestController
public class ClubController {

  private static final Logger log = LoggerFactory.getLogger(ClubController.class);

  /**
   * retrieve all players from the requested club with a ranking in the current ranking period.
   *
   * @param name name of the club
   * @return player data
   */
  @Operation(summary = "get players for specified club")
  @GetMapping(path = "/clubs/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ClubPlayer> getRequestedClubsPlayers(
      @Parameter(description = "name of the club")
      @PathVariable(value = "name") String name) {

    log.debug("requesting players for club {}", name);
    return Mono.fromCallable(() -> null);
  }

  /**
   * search for a club by a given string.
   *
   * @param name name part to search for
   * @return club search result.
   */
  @Operation(summary = "search for a club by its name")
  @GetMapping(path = "/clubs", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ClubSearchResult> searchClubs(@Parameter(description = "name part of the club to search")
                                                @RequestParam(value = "name", required = false) String name) {
    log.debug("performing club search using parameter name: {}", name);
    return Mono.fromCallable(() -> null);
  }
}
