package de.hdawg.rankinginfo.app.rest;

import de.hdawg.rankinginfo.app.model.club.ClubPlayerResult;
import de.hdawg.rankinginfo.app.model.club.ClubSearchResult;
import de.hdawg.rankinginfo.app.services.ClubService;
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
 * Endpoints for club data.
 */
@RestController
@RequestMapping("/api")
public class ClubController {

  private final ClubService clubService;

  public ClubController(ClubService clubService) {
    this.clubService = clubService;
  }

  /**
   * retrieve all players from the requested club with a ranking in the current ranking period.
   *
   * @param name name of the club
   * @return player data
   */
  @Operation(summary = "get players for specified club", tags = "club")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "successfully received the requested player data.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ClubPlayerResult.class))})
  })
  @GetMapping(path = "/clubs/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ClubPlayerResult> getRequestedClubsPlayers(
      @Parameter(in = ParameterIn.PATH, description = "name of the club")
      @PathVariable(value = "name") String name) {

    return ResponseEntity.ok(clubService.getPlayersForClub(name));
  }

  /**
   * search for a club by a given string.
   *
   * @param name name part to search for
   * @return club search result.
   */
  @Operation(summary = "search for a club by its name", tags = "club")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "successfully received the requested player data.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ClubSearchResult.class))})
  })
  @GetMapping(path = "/clubs", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ClubSearchResult> searchClubs(
      @Parameter(in = ParameterIn.QUERY, description = "name part of the club to search")
      @RequestParam(value = "name", required = false) String name) {
    return ResponseEntity.ok(clubService.findClub(name));
  }
}
