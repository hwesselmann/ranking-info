package de.hdawg.rankinginfo.api.v1

import de.hdawg.rankinginfo.repository.RankingRepository
import de.hdawg.rankinginfo.service.PlayerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller exposing player search and player detail endpoints under `/api/v1/players`.
 *
 * All endpoints require a valid Bearer token (see [de.hdawg.rankinginfo.api.security.BearerTokenFilter]).
 */
@RestController
@RequestMapping("/api/v1/players")
@SecurityRequirement(name = "bearerAuth")
class PlayersApiController(
    private val playerService: PlayerService,
    private val rankingRepository: RankingRepository,
) {
    /**
     * Searches players by last name, optionally filtered by year of birth.
     *
     * @param lastname player's last name (required); returns 400 if blank
     * @param yob four-digit year of birth (e.g. "2005"); optional
     * @return [PlayerSearchResponse] on success, [ErrorResponse] on 400/404
     */
    @Operation(summary = "Search players by lastname and optional year of birth")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = PlayerSearchResponse::class))]),
        ApiResponse(responseCode = "400", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    )
    @GetMapping
    fun search(
        @RequestParam(required = false) lastname: String?,
        @RequestParam(required = false) yob: String?,
    ): ResponseEntity<Any> {
        if (lastname.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(ErrorResponse("lastname parameter required"))
        }
        val players =
            if (!yob.isNullOrBlank()) {
                val yobMale = yob.substring(2, 4).toInt() + 100
                playerService.findPlayersByLastnameAndYob(lastname, yobMale, yobMale + 100)
            } else {
                playerService.findPlayersByLastname(lastname)
            }
        if (players.isEmpty()) {
            return ResponseEntity.status(404).body(ErrorResponse("Not Found"))
        }
        return ResponseEntity.ok(
            PlayerSearchResponse(
                request = PlayerSearchRequest(lastname = lastname, yob = yob, total_count = players.size),
                data =
                    players.map { p ->
                        PlayerSearchItem(
                            dtb_id = p.dtbId,
                            lastname = p.lastname,
                            firstname = p.firstname,
                            club = p.club,
                            links = PlayerLink(self = "/api/v1/players/${p.dtbId}"),
                        )
                    },
            ),
        )
    }

    /**
     * Returns the full profile and ranking history for a single player.
     *
     * @param id the player's DTB ID
     * @return [PlayerDetailResponse] on success, [ErrorResponse] with 404 if not found
     */
    @Operation(summary = "Get player profile with full ranking history")
    @ApiResponses(
        ApiResponse(responseCode = "200", content = [Content(schema = Schema(implementation = PlayerDetailResponse::class))]),
        ApiResponse(responseCode = "404", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    )
    @GetMapping("/{id}")
    fun show(
        @PathVariable id: Int,
    ): ResponseEntity<Any> {
        val rankings =
            rankingRepository
                .findByDtbIdAndYobRankingFalseAndAgeGroupRankingFalseAndYearEndRankingFalseOrderByDateDescAgeGroupAsc(id)
        if (rankings.isEmpty()) {
            return ResponseEntity.status(404).body(ErrorResponse("Not Found"))
        }
        val current = rankings.first()
        return ResponseEntity.ok(
            PlayerDetailResponse(
                data =
                    PlayerDetailData(
                        dtb_id = current.dtbId,
                        lastname = current.lastname,
                        firstname = current.firstname,
                        nationality = current.nationality,
                        club = current.club,
                        federation = current.federation,
                        rankings =
                            rankings.map { r ->
                                RankingEntry(
                                    quarter = r.date,
                                    age_group = r.ageGroup,
                                    ranking_position = r.rankingPosition,
                                    score = r.score,
                                )
                            },
                    ),
            ),
        )
    }
}
