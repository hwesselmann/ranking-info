package de.hdawg.rankinginfo.service.controller;

import de.hdawg.rankinginfo.service.model.player.Player;
import de.hdawg.rankinginfo.service.services.PlayerService;
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

@RestController
public class PlayerController {

  private static final Logger log = LoggerFactory.getLogger(PlayerController.class);

  private final PlayerService playerService;

  public PlayerController(PlayerService playerService) {
    this.playerService = playerService;
  }

  @Operation(summary = "get player data for the given dtbid")
  @GetMapping(path = "/players/{dtbid}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Player> getPlayerForDtbId(@Parameter(description = "DTB-ID of the player to fetch")
                                        @PathVariable(value = "dtbid") String dtbId) {
    return null;
  }

  @Operation(summary = "search for a player by dtbId, year of birth an/or lastname")
  @GetMapping(path = "/players", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Player> searchPlayers(@Parameter(description = "DTB-ID to query", required = false)
                                    @RequestParam(value = "dtbid") String dtbId,
                                    @Parameter(description = "name to query", required = false)
                                    @RequestParam(value = "name") String lastname,
                                    @Parameter(description = "year of birth to query", required = false)
                                    @RequestParam(value = "yob") String yob) {
    return null;
  }
}
