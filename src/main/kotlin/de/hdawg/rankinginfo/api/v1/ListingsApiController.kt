package de.hdawg.rankinginfo.api.v1

import de.hdawg.rankinginfo.service.RankingFilter
import de.hdawg.rankinginfo.service.RankingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.MessageDigest

/**
 * REST controller exposing paginated ranking listings under `/api/v1/listings`.
 *
 * Supports ETag-based caching: returns HTTP 304 when the client's `If-None-Match`
 * header matches the current ETag derived from the latest import timestamp and filter parameters.
 *
 * All endpoints require a valid Bearer token (see [de.hdawg.rankinginfo.api.security.BearerTokenFilter]).
 */
@RestController
@RequestMapping("/api/v1/listings")
@SecurityRequirement(name = "bearerAuth")
class ListingsApiController(
    private val rankingService: RankingService,
) {
    companion object {
        private const val MAX_PER_PAGE = 100
    }

    /**
     * Returns a paginated, optionally filtered ranking list for the given quarter and age group.
     *
     * @param quarter ISO date string representing the ranking period (e.g. "2024-01-01")
     * @param ageGroupSlug slug identifying gender and age group (e.g. "m12", "wu18", "m00")
     * @param page 1-based page number (default: 1)
     * @param perPage results per page; capped at [MAX_PER_PAGE] (default: 25)
     * @param federation optional federation filter
     * @param club optional club name filter (case-insensitive substring match)
     * @param ageGroupOptions optional age group variant ("only_yob" or "include_younger")
     * @param yearEnd when true, returns year-end rankings (only valid for January quarters)
     * @param request used to read `If-None-Match` for ETag validation
     * @return [ListingResponse] with an `ETag` header, or HTTP 304 when content is unchanged
     */
    @Operation(summary = "Get paginated ranking list for a quarter and age group")
    @GetMapping("/{quarter}/{ageGroupSlug}")
    fun index(
        @PathVariable quarter: String,
        @PathVariable ageGroupSlug: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "25") perPage: Int,
        @RequestParam(required = false) federation: String?,
        @RequestParam(required = false) club: String?,
        @RequestParam(name = "age_group_options", required = false) ageGroupOptions: String?,
        @RequestParam(name = "year_end", defaultValue = "false") yearEnd: Boolean,
        request: HttpServletRequest,
    ): ResponseEntity<ListingResponse> {
        val filter =
            RankingFilter(
                quarter = quarter,
                ageGroupSlug = ageGroupSlug,
                ageGroupOptions = ageGroupOptions,
                federation = federation,
                club = club,
                yearEnd = yearEnd,
            )
        val cappedPerPage = minOf(perPage, MAX_PER_PAGE)

        val maxImportedAt = rankingService.maxImportedAt()
        val etagSource = "$maxImportedAt|$quarter|$ageGroupSlug|$ageGroupOptions|$federation|$club|$yearEnd|$page|$cappedPerPage"
        val etag = "\"${sha256(etagSource)}\""
        val ifNoneMatch = request.getHeader("If-None-Match")
        if (ifNoneMatch == etag) {
            return ResponseEntity.status(304).build()
        }

        val rankings = rankingService.findFilteredRankings(filter, page, cappedPerPage)
        val dtbIds = rankings.content.map { it.dtbId }
        val prevPositions = rankingService.findPreviousPositions(filter, dtbIds)

        val response =
            ListingResponse(
                request =
                    ListingRequest(
                        quarter = quarter,
                        age_group = ageGroupSlug,
                        age_group_options = ageGroupOptions,
                        federation = federation,
                        club = club,
                        year_end = yearEnd,
                        page = page,
                        per_page = cappedPerPage,
                        total_count = rankings.totalElements,
                    ),
                data =
                    rankings.content.map { r ->
                        ListingItem(
                            dtb_id = r.dtbId,
                            ranking_position = r.rankingPosition,
                            lastname = r.lastname,
                            firstname = r.firstname,
                            nationality = r.nationality,
                            club = r.club,
                            federation = r.federation,
                            score = r.score,
                            position_change = prevPositions[r.dtbId]?.let { it - r.rankingPosition },
                            links = PlayerLink(self = "/api/v1/players/${r.dtbId}"),
                        )
                    },
            )

        return ResponseEntity
            .ok()
            .header("ETag", etag)
            .body(response)
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }
}
