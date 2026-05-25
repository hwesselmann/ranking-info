package de.hdawg.rankinginfo.web

import de.hdawg.rankinginfo.repository.RankingRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Web controller for club browsing views at `/clubs`.
 *
 * The index page allows searching clubs by name; the detail page shows all ranked
 * players (adults and youth) currently affiliated with the given club.
 */
@Controller
@RequestMapping("/clubs")
class ClubController(
    private val rankingRepository: RankingRepository,
) {
    companion object {
        private val ADULT_LABELS = mapOf("m00" to "Herren", "w00" to "Damen")
    }

    /**
     * Shows a club search form. When [commit] is set and [club] is not blank,
     * queries the latest quarter for matching clubs and adds them to the model.
     */
    @GetMapping
    fun index(
        @RequestParam(required = false) commit: String?,
        @RequestParam(required = false) club: String?,
        model: Model,
    ): String {
        model.addAttribute("params", mapOf("club" to club))
        if (commit != null && !club.isNullOrBlank()) {
            val quarter = rankingRepository.findDistinctDatesDesc().firstOrNull()
            if (quarter != null) {
                val youthByClub =
                    rankingRepository
                        .findByDateAndAgeGroupAndClubContaining(quarter, "overall", club)
                        .groupBy { it.club }
                val adultByClub =
                    (
                        rankingRepository.findByDateAndAgeGroupAndClubContaining(quarter, "m00", club) +
                            rankingRepository.findByDateAndAgeGroupAndClubContaining(quarter, "w00", club)
                    ).groupBy { it.club }
                val clubs =
                    (youthByClub.keys + adultByClub.keys)
                        .distinct()
                        .sorted()
                        .map { name ->
                            mapOf(
                                "name" to name,
                                "youth_count" to (youthByClub[name]?.size ?: 0),
                                "adult_count" to (adultByClub[name]?.size ?: 0),
                            )
                        }
                model.addAttribute("clubs", clubs)
            }
        }
        return "clubs/index"
    }

    /**
     * Shows the detail page for a specific club, displaying all currently ranked players
     * grouped by age group (U12–U18, Herren, Damen).
     *
     * @param id exact club name as stored in the rankings
     */
    @GetMapping("/{id}")
    fun show(
        @PathVariable id: String,
        model: Model,
    ): String {
        val quarter = rankingRepository.findDistinctDatesDesc().firstOrNull()
        val players = mutableMapOf<String, Any>()
        if (quarter != null) {
            val youth =
                rankingRepository
                    .findByDateAndAgeGroupAndClubContaining(quarter, "overall", id)
                    .filter { it.club.equals(id, ignoreCase = true) }

            for ((ag, label) in ADULT_LABELS) {
                val adults =
                    rankingRepository
                        .findByDateAndAgeGroupAndClubContaining(quarter, ag, id)
                        .filter { it.club.equals(id, ignoreCase = true) }
                if (adults.isNotEmpty()) {
                    players[label] =
                        adults.map {
                            mapOf(
                                "dtb_id" to it.dtbId,
                                "lastname" to it.lastname,
                                "firstname" to it.firstname,
                                "rank" to it.rankingPosition,
                                "score" to it.score,
                            )
                        }
                }
            }

            val dtbIds = youth.map { it.dtbId }.toSet()
            if (dtbIds.isNotEmpty()) {
                val ageGroupRankings =
                    rankingRepository
                        .findAgeGroupRankingsByDateAndDtbIds(quarter, dtbIds)
                        .associateBy { it.dtbId }
                val grouped = youth.groupBy { ageGroupRankings[it.dtbId]?.ageGroup }
                for (ag in listOf("U12", "U14", "U16", "U18")) {
                    val group = grouped[ag] ?: continue
                    players[ag] =
                        group.sortedBy { it.rankingPosition }.map {
                            mapOf(
                                "dtb_id" to it.dtbId,
                                "lastname" to it.lastname,
                                "firstname" to it.firstname,
                                "rank" to it.rankingPosition,
                                "score" to it.score,
                                "age" to ag,
                            )
                        }
                }
            }
        }
        model.addAttribute("clubName", id)
        model.addAttribute("players", players)
        return "clubs/show"
    }
}
