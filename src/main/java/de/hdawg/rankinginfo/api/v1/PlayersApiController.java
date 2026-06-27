package de.hdawg.rankinginfo.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.hdawg.rankinginfo.api.v1.dto.PlayerDetailData;
import de.hdawg.rankinginfo.api.v1.dto.PlayerDetailResponse;
import de.hdawg.rankinginfo.api.v1.dto.PlayerLink;
import de.hdawg.rankinginfo.api.v1.dto.PlayerSearchItem;
import de.hdawg.rankinginfo.api.v1.dto.PlayerSearchRequest;
import de.hdawg.rankinginfo.api.v1.dto.PlayerSearchResponse;
import de.hdawg.rankinginfo.api.v1.dto.RankingEntry;
import de.hdawg.rankinginfo.domain.RankingCoding;
import de.hdawg.rankinginfo.service.PlayerService;

@RestController
@RequestMapping("/api/v1/players")
@SecurityRequirement(name = "bearerAuth")
public class PlayersApiController {

  private final PlayerService playerService;

  public PlayersApiController(PlayerService playerService) {
    this.playerService = playerService;
  }

  @Operation(summary = "Search players by lastname and optional year of birth")
  @ApiResponse(
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = PlayerSearchResponse.class)))
  @ApiResponse(
      responseCode = "400",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  @ApiResponse(
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  @GetMapping
  public ResponseEntity<Object> search(
      @RequestParam(required = false, defaultValue = "") String lastname,
      @RequestParam(required = false, defaultValue = "") String yob) {

    if (lastname.isBlank()) {
      return ResponseEntity.badRequest()
          .body(
              ProblemDetail.forStatusAndDetail(
                  HttpStatus.BAD_REQUEST, "lastname parameter required"));
    }

    if (!yob.isBlank() && yob.length() != 4) {
      return ResponseEntity.badRequest()
          .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "yob must be a 4-digit year"));
    }

    int yy = yob.isBlank() ? 0 : Integer.parseInt(yob.substring(2, 4));
    var players =
        (!yob.isBlank())
            ? playerService.findPlayersByLastnameAndYob(
                lastname,
                yy + RankingCoding.GENDER_FACTOR_JUNIOREN,
                yy + RankingCoding.GENDER_FACTOR_JUNIORINNEN)
            : playerService.findPlayersByLastname(lastname);

    if (players.isEmpty()) {
      return ResponseEntity.status(404)
          .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Not Found"));
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
  @ApiResponse(
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = PlayerDetailResponse.class)))
  @ApiResponse(
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  @GetMapping("/{id}")
  public ResponseEntity<Object> show(@PathVariable int id) {
    var rankings = playerService.findNonAggregateRankings(id);
    if (rankings.isEmpty()) {
      return ResponseEntity.status(404)
          .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Not Found"));
    }
    var current = rankings.getFirst();
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
