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

import de.hdawg.rankinginfo.api.v1.dto.ClubDetailData;
import de.hdawg.rankinginfo.api.v1.dto.ClubDetailResponse;
import de.hdawg.rankinginfo.api.v1.dto.ClubGroup;
import de.hdawg.rankinginfo.api.v1.dto.ClubPlayerItem;
import de.hdawg.rankinginfo.api.v1.dto.ClubSearchItem;
import de.hdawg.rankinginfo.api.v1.dto.ClubSearchRequest;
import de.hdawg.rankinginfo.api.v1.dto.ClubSearchResponse;
import de.hdawg.rankinginfo.api.v1.dto.PlayerLink;
import de.hdawg.rankinginfo.web.ClubService;

@RestController
@RequestMapping("/api/v1/clubs")
@SecurityRequirement(name = "bearerAuth")
public class ClubsApiController {

  private final ClubService clubService;

  public ClubsApiController(ClubService clubService) {
    this.clubService = clubService;
  }

  @Operation(summary = "Search clubs by name")
  @ApiResponse(
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = ClubSearchResponse.class)))
  @ApiResponse(
      responseCode = "400",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  @ApiResponse(
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  @GetMapping
  public ResponseEntity<Object> search(@RequestParam(required = false, defaultValue = "") String name) {
    if (name.isBlank()) {
      return ResponseEntity.badRequest()
          .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "name parameter required"));
    }

    var clubs = clubService.searchClubs(name);
    if (clubs.isEmpty()) {
      return ResponseEntity.status(404)
          .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Not Found"));
    }

    var data =
        clubs.stream()
            .map(c -> new ClubSearchItem(c.name(), c.youthCount(), c.adultCount()))
            .toList();

    return ResponseEntity.ok(new ClubSearchResponse(new ClubSearchRequest(name, clubs.size()), data));
  }

  @Operation(summary = "Get club roster grouped by category and age group")
  @ApiResponse(
      responseCode = "200",
      content = @Content(schema = @Schema(implementation = ClubDetailResponse.class)))
  @ApiResponse(
      responseCode = "404",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
  @GetMapping("/{id}")
  public ResponseEntity<Object> show(@PathVariable String id) {
    var roster = clubService.getClubDetail(id);
    if (roster.isEmpty()) {
      return ResponseEntity.status(404)
          .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Not Found"));
    }

    var groups =
        roster.entrySet().stream()
            .map(
                e ->
                    new ClubGroup(
                        e.getKey(),
                        e.getValue().stream()
                            .map(
                                p ->
                                    new ClubPlayerItem(
                                        p.dtbId(),
                                        p.lastname(),
                                        p.firstname(),
                                        p.rank(),
                                        p.score(),
                                        new PlayerLink("/api/v1/players/" + p.dtbId())))
                            .toList()))
            .toList();

    return ResponseEntity.ok(new ClubDetailResponse(new ClubDetailData(id, groups)));
  }
}
