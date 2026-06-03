package de.hdawg.rankinginfo.web

import de.hdawg.rankinginfo.service.RankingFilter
import de.hdawg.rankinginfo.service.RankingService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Web controller for the ranking listings view at `/listings`.
 *
 * Renders a filterable ranking list based on quarter, gender, age group,
 * federation, club, and year-end flag. Position changes relative to the
 * previous quarter are calculated and passed to the view.
 */
@Controller
@RequestMapping("/listings")
class ListingController(
    private val rankingService: RankingService,
) {
    @GetMapping
    fun index(
        @RequestParam(required = false) quarter: String?,
        @RequestParam(required = false) gender: String?,
        @RequestParam(name = "age_group", required = false) ageGroup: String?,
        @RequestParam(name = "age_group_options", required = false) ageGroupOptions: String?,
        @RequestParam(required = false) federation: String?,
        @RequestParam(required = false) club: String?,
        @RequestParam(name = "year_end", defaultValue = "0") yearEnd: String,
        @RequestParam(required = false) commit: String?,
        model: Model,
    ): String {
        val quarters = rankingService.fetchAvailableQuarters()
        val federations = rankingService.fetchFederations()
        model.addAttribute("quarters", quarters)
        model.addAttribute("federations", federations)
        model.addAttribute(
            "params",
            mapOf(
                "quarter" to quarter,
                "gender" to gender,
                "age_group" to ageGroup,
                "age_group_options" to ageGroupOptions,
                "federation" to federation,
                "club" to club,
                "year_end" to yearEnd,
            ),
        )

        if (commit != null && !quarter.isNullOrBlank() && !gender.isNullOrBlank()) {
            val slug = toAgeGroupSlug(gender, ageGroup)
            val filter =
                RankingFilter(
                    quarter,
                    slug,
                    ageGroupOptions?.takeIf { it.isNotBlank() },
                    federation,
                    club,
                    yearEnd == "1",
                )
            val rankings = rankingService.findFilteredRankings(filter, 1, 1000)
            val dtbIds = rankings.content.map { it.dtbId }
            val prevPositions = rankingService.findPreviousPositions(filter, dtbIds)
            model.addAttribute("rankings", rankings.content)
            model.addAttribute("previousPositions", prevPositions)
        }
        return "listing/index"
    }

    private fun toAgeGroupSlug(
        gender: String,
        ageGroup: String?,
    ): String =
        when (gender) {
            "Herren" -> "m00"
            "Damen" -> "w00"
            "Junioren" -> if (ageGroup.isNullOrBlank()) "overall" else "m${ageGroup.lowercase()}"
            "Juniorinnen" -> if (ageGroup.isNullOrBlank()) "overall" else "w${ageGroup.lowercase()}"
            else -> "overall"
        }
}
