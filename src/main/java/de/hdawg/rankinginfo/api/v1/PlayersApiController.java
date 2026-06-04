package de.hdawg.rankinginfo.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.hdawg.rankinginfo.repository.RankingRepository;
import de.hdawg.rankinginfo.service.PlayerService;

@RestController
@RequestMapping("/api/v1/players")
@SecurityRequirement(name = "bearerAuth")
public class PlayersApiController {

  private final PlayerService playerService;
  private final RankingRepository rankingRepository;

  public PlayersApiController(PlayerService playerService, RankingRepository rankingRepository) {
    this.playerService = playerService;
    this.rankingRepository = rankingRepository;
  }

  @Operation(summary = "Search players by lastname and optional year of birth")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = PlayerSearchResponse.class))),
    @ApiResponse(
        responseCode = "400",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping
  public ResponseEntity<Object> search(
      @RequestParam(required = false) String lastname,
      @RequestParam(required = false) String yob) {

    if (lastname == null || lastname.isBlank()) {
      return ResponseEntity.badRequest().body(new ErrorResponse("lastname parameter required"));
    }

    var players =
        (yob != null && !yob.isBlank())
            ? playerService.findPlayersByLastnameAndYob(
                lastname,
                Integer.parseInt(yob.substring(2, 4)) + 100,
                Integer.parseInt(yob.substring(2, 4)) + 200)
            : playerService.findPlayersByLastname(lastname);

    if (players.isEmpty()) {
      return ResponseEntity.status(404).body(new ErrorResponse("Not Found"));
    }

    var data =
        players.stream()
            .map(
                p ->
                    new PlayerSearchItem(
                        p.dtbId(),
                        p.lastname(),
                        p.firstname(),
                        p.club(),
                        new PlayerLink("/api/v1/players/" + p.dtbId())))
            .toList();

    return ResponseEntity.ok(
        new PlayerSearchResponse(
            new PlayerSearchRequest(lastname, yob, players.size()), data));
  }

  @Operation(summary = "Get player profile with full ranking history")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        content = @Content(schema = @Schema(implementation = PlayerDetailResponse.class))),
    @ApiResponse(
        responseCode = "404",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<Object> show(@PathVariable int id) {
    var rankings =
        rankingRepository
            .findByDtbIdAndYobRankingFalseAndAgeGroupRankingFalseAndYearEndRankingFalseOrderByDateDescAgeGroupAsc(
                id);
    if (rankings.isEmpty()) {
      return ResponseEntity.status(404).body(new ErrorResponse("Not Found"));
    }
    var current = rankings.get(0);
    var rankingEntries =
        rankings.stream()
            .map(
                r ->
                    new RankingEntry(
                        r.date(), r.ageGroup(), r.rankingPosition(), r.score()))
            .toList();

    return ResponseEntity.ok(
        new PlayerDetailResponse(
            new PlayerDetailData(
                current.dtbId(),
                current.lastname(),
                current.firstname(),
                current.nationality(),
                current.club(),
                current.federation(),
                rankingEntries)));
  }
}
