package de.hdawg.rankinginfo.rest.controller;

import de.hdawg.rankinginfo.rest.model.status.AvailableRankingPeriods;
import de.hdawg.rankinginfo.rest.services.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for status data.
 */
@RestController
public class StatusController {

  private final StatusService statusService;

  public StatusController(StatusService statusService) {
    this.statusService = statusService;
  }

  /**
   * retrieve all available ranking periods.
   *
   * @return ranking periods
   */
  @Operation(summary = "get rankings periods")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "successfully received available ranking periods.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = AvailableRankingPeriods.class))})
  })
  @GetMapping(path = "/status/rankingperiods", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AvailableRankingPeriods> getAvailableRankingPeriods() {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
        .body(statusService.getAvailableRankingPeriods());
  }

  /**
   * retrieve the most recent available ranking period.
   *
   * @return most recent ranking period
   */
  @Operation(summary = "get most recent rankings period")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "successfully received the current ranking period.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = LocalDate.class))})
  })
  @GetMapping(path = "/status/rankingperiods/current", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LocalDate> getMostRecentRankingPeriod() {
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
        .body(statusService.getMostRecentRankingPeriod());
  }
}
