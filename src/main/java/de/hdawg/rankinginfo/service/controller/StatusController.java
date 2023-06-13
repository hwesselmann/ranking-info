package de.hdawg.rankinginfo.service.controller;

import de.hdawg.rankinginfo.service.model.status.AvailableRankingPeriods;
import de.hdawg.rankinginfo.service.services.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
  @GetMapping(path = "/status/rankingperiods", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<AvailableRankingPeriods> getAvailableRankingPeriods() {
    return Mono.fromCallable(statusService::getAvailableRankingPeriods);
  }
}
