package de.hdawg.rankinginfo.app.rest;

import de.hdawg.rankinginfo.app.model.player.Player;
import de.hdawg.rankinginfo.app.model.player.PlayerSearchResult;
import de.hdawg.rankinginfo.app.services.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * controller for player endpoints.
 */
@RestController
@RequestMapping("/api")
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
  @Operation(summary = "get player data for the given dtbId", tags = "player")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "successfully received the requested player data.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = Player.class))}),
      @ApiResponse(responseCode = "400", description = "unknown id passed for request", content = @Content)
  })
  @GetMapping(path = "/players/{dtbid}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Player> getPlayerForDtbId(
      @Parameter(in = ParameterIn.PATH, description = "DTB-ID of the player to fetch")
      @PathVariable(value = "dtbid") String dtbId) {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
        .body(playerService.getPlayerById(dtbId));
  }

  /**
   * search for available players by dtbId, name an/or year of birth.
   *
   * @param dtbId unique player id (or part of it)
   * @param name  lastname (or partial)
   * @param yob   year of birth
   * @return list of found players or empty list
   */
  @Operation(summary = "search for a player by dtbId, year of birth an/or lastname", tags = "player")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "successfully received the requested search result.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PlayerSearchResult.class))})
  })
  @GetMapping(path = "/players", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PlayerSearchResult> searchPlayers(
      @Parameter(in = ParameterIn.QUERY, description = "DTB-ID to query")
      @RequestParam(value = "dtbid", required = false)
      String dtbId,
      @Parameter(in = ParameterIn.QUERY, description = "name to query")
      @RequestParam(value = "name", required = false)
      String name,
      @Parameter(in = ParameterIn.QUERY, description = "year of birth to query")
      @RequestParam(value = "yob", required = false)
      String yob) {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
        .body(playerService.findPlayers(dtbId, name, yob));
  }
}
