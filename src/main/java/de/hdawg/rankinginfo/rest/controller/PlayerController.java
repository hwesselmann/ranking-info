package de.hdawg.rankinginfo.rest.controller;

import de.hdawg.rankinginfo.rest.model.player.Player;
import de.hdawg.rankinginfo.rest.model.player.PlayerSearchResult;
import de.hdawg.rankinginfo.rest.services.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * controller for player endpoints.
 */
@RestController
public class PlayerController {

  private final PlayerService playerService;

  public PlayerController(PlayerService playerService) {
    this.playerService = playerService;
  }

  /**
   * fetch a player by the unique id.
   *
   * @param dtbId unique player id
   * @return player domain object
   */
  @Operation(summary = "get player data for the given dtbId")
  @GetMapping(path = "/players/{dtbid}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Player> getPlayerForDtbId(
      @Parameter(in = ParameterIn.PATH, description = "DTB-ID of the player to fetch")
      @PathVariable(value = "dtbid") String dtbId) {
    return Mono.fromCallable(() -> playerService.getPlayerById(dtbId));
  }

  /**
   * search for available players by dtbId, name an/or year of birth.
   *
   * @param dtbId unique player id (or part of it)
   * @param name  lastname (or partial)
   * @param yob   year of birth
   * @return list of found players or empty list
   */
  @Operation(summary = "search for a player by dtbId, year of birth an/or lastname")
  @GetMapping(path = "/players", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<PlayerSearchResult> searchPlayers(
      @Parameter(in = ParameterIn.QUERY, description = "DTB-ID to query")
      @RequestParam(value = "dtbid", required = false)
      String dtbId,
      @Parameter(in = ParameterIn.QUERY, description = "name to query")
      @RequestParam(value = "name", required = false)
      String name,
      @Parameter(in = ParameterIn.QUERY, description = "year of birth to query")
      @RequestParam(value = "yob", required = false)
      String yob) {
    return Mono.fromCallable(() -> playerService.findPlayers(dtbId, name, yob));
  }
}
