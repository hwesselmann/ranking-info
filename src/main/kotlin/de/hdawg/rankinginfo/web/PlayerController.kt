package de.hdawg.rankinginfo.web

import de.hdawg.rankinginfo.repository.RankingRepository
import de.hdawg.rankinginfo.service.PlayerService
import de.hdawg.rankinginfo.service.RankingService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.time.format.DateTimeFormatter

/**
 * Web controller for player search and player detail views at `/players` and `/player/{id}`.
 *
 * The index action supports searching by last name, year of birth, or DTB ID.
 * Single-result searches redirect directly to the player detail page.
 * The detail page shows current rankings, full ranking history, and chart data.
 */
@Controller
@RequestMapping("/players")
class PlayerController(
    private val playerService: PlayerService,
    private val rankingService: RankingService,
    private val rankingRepository: RankingRepository,
) {
    private val quarterMap = mapOf(1 to "Q1", 4 to "Q2", 7 to "Q3", 10 to "Q4")
    private val dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    @GetMapping
    fun index(
        @RequestParam(required = false) lastname: String?,
        @RequestParam(required = false) yob: String?,
        @RequestParam(name = "dtb_id", required = false) dtbId: String?,
        @RequestParam(required = false) commit: String?,
        model: Model,
        redirect: RedirectAttributes,
    ): String {
        when {
            !dtbId.isNullOrBlank() -> {
                val idNum = dtbId.trim().toLongOrNull() ?: 0L
                val missing = 8 - idNum.toString().length
                val start = (idNum * Math.pow(10.0, missing.toDouble())).toInt()
                val end = start + Math.pow(10.0, missing.toDouble()).toInt() - 1
                val players = playerService.findPlayersByDtbIdRange(start, end)
                if (players.size == 1) return "redirect:/player/${players[0].dtbId}"
                model.addAttribute("players", players)
            }
            !lastname.isNullOrBlank() && !yob.isNullOrBlank() -> {
                val yobMale = yob.trim().substring(2, 4).toInt() + 100
                val players = playerService.findPlayersByLastnameAndYob(lastname.trim(), yobMale, yobMale + 100)
                if (players.size == 1) return "redirect:/player/${players[0].dtbId}"
                model.addAttribute("players", players)
            }
            !lastname.isNullOrBlank() -> {
                val players = playerService.findPlayersByLastname(lastname.trim())
                if (players.size == 1) return "redirect:/player/${players[0].dtbId}"
                model.addAttribute("players", players)
            }
            !yob.isNullOrBlank() -> {
                val yobMale = yob.trim().substring(2, 4).toInt() + 100
                val players = playerService.findPlayersByYob(yobMale, yobMale + 100)
                if (players.size == 1) return "redirect:/player/${players[0].dtbId}"
                model.addAttribute("players", players)
            }
        }
        model.addAttribute("params", mapOf("lastname" to lastname, "yob" to yob, "dtb_id" to dtbId))
        return "players/index"
    }

    @GetMapping("/{id}")
    fun show(
        @PathVariable id: Int,
        model: Model,
        redirect: RedirectAttributes,
    ): String =
        try {
            val player = playerService.loadPlayerProfile(id)

            val rawRankings =
                rankingRepository
                    .findByDtbIdAndYobRankingFalseAndAgeGroupRankingFalseAndYearEndRankingFalseOrderByDateDescAgeGroupAsc(id)
            val fullRankings =
                rankingRepository
                    .findByDtbIdAndYearEndRankingFalseOrderByDateAscAgeGroupAsc(id)

            val allDates = rankingRepository.findAllDistinctDatesDesc()
            val currentQuarter = allDates.firstOrNull()
            val previousQuarter = allDates.getOrNull(1)
            val recent4Dates = allDates.take(4).toSet()

            val currentRankings =
                if (currentQuarter != null) {
                    rawRankings
                        .filter { it.date == currentQuarter && it.ageGroup != "overall" }
                        .map { r ->
                            val prev =
                                if (previousQuarter != null) {
                                    rawRankings.find { it.date == previousQuarter && it.ageGroup == r.ageGroup }
                                } else {
                                    null
                                }
                            mapOf(
                                "age_group" to r.ageGroup,
                                "position" to r.rankingPosition,
                                "score" to r.score,
                                "position_change" to
                                    prev?.let {
                                        val diff = it.rankingPosition - r.rankingPosition
                                        if (diff > 0) "+$diff" else diff.toString()
                                    },
                                "score_change" to
                                    prev?.let {
                                        val diff =
                                            r.score
                                                .replace(',', '.')
                                                .toDoubleOrNull()
                                                ?.minus(it.score.replace(',', '.').toDoubleOrNull() ?: 0.0)
                                        if (diff != null && diff != 0.0) (if (diff > 0) "+$diff" else diff.toString()) else null
                                    },
                            )
                        }.sortedWith(
                            compareBy({ if (listOf("m00", "w00").contains(it["age_group"])) 0 else 1 }, { it["age_group"] as String }),
                        )
                } else {
                    emptyList()
                }

            val allTimeDiagram = buildDiagramData(fullRankings)
            val recent12mDiagram = buildDiagramData(fullRankings.filter { it.date in recent4Dates })
            val completeRankings = buildCompleteRankings(fullRankings)
            val availableData = buildAvailableData(completeRankings)

            model.addAttribute("player", player)
            model.addAttribute("currentRankings", currentRankings)
            model.addAttribute("completeRankings", completeRankings)
            model.addAttribute("diagramData", allTimeDiagram["positions"])
            model.addAttribute("diagramScoreData", allTimeDiagram["scores"])
            model.addAttribute("recent12mData", recent12mDiagram["positions"])
            model.addAttribute("recent12mScoreData", recent12mDiagram["scores"])
            model.addAttribute("availableData", availableData)
            "players/show"
        } catch (e: Exception) {
            redirect.addFlashAttribute("danger", "Spieler nicht gefunden")
            "redirect:/players"
        }

    private fun buildCompleteRankings(rankings: List<de.hdawg.rankinginfo.domain.Ranking>): List<Map<String, Any?>> {
        val skipGroups = setOf("overall")
        val byDate =
            rankings
                .filter { it.ageGroup !in skipGroups }
                .groupBy { it.date }
                .toSortedMap(reverseOrder())
        return byDate.mapNotNull { (date, rows) ->
            val quarter = quarterMap[date.monthValue] ?: return@mapNotNull null
            val map =
                mutableMapOf<String, Any?>(
                    "date" to "${date.year}/$quarter",
                    "score" to (rows.firstOrNull()?.score ?: ""),
                )
            for (r in rows) {
                val key = if (r.ageGroup in listOf("m00", "w00")) "Aktive" else r.ageGroup
                map[key] = r.rankingPosition
            }
            map as Map<String, Any?>
        }
    }

    private fun buildDiagramData(rankings: List<de.hdawg.rankinginfo.domain.Ranking>): Map<String, Any> {
        val validAgeGroups = listOf("U12", "U14", "U16", "U18", "m00", "w00")
        val positions = linkedMapOf<String, LinkedHashMap<String, Int>>()
        val scores = linkedMapOf<String, String>()
        for (r in rankings) {
            if (r.ageGroup !in validAgeGroups) continue
            val groupKey = if (r.ageGroup in listOf("m00", "w00")) "Aktive" else r.ageGroup
            val period = r.date.minusDays(1).format(dtf)
            positions.getOrPut(groupKey) { linkedMapOf() }[period] = r.rankingPosition
            scores.putIfAbsent(period, r.score)
        }
        val positionsList =
            listOf("U12", "U14", "U16", "U18", "Aktive")
                .mapNotNull { ag -> positions[ag]?.let { mapOf("name" to ag, "data" to it) } }
        return mapOf(
            "positions" to positionsList,
            "scores" to listOf(mapOf("name" to "Punkte", "data" to scores)),
        )
    }

    private fun buildAvailableData(completeRankings: List<Map<String, Any?>>): Map<Int, Set<String>> {
        val result = sortedMapOf<Int, MutableSet<String>>(reverseOrder())
        for (row in completeRankings) {
            val dateStr = row["date"] as? String ?: continue
            val parts = dateStr.split("/")
            if (parts.size != 2) continue
            val year = parts[0].toIntOrNull() ?: continue
            result.getOrPut(year) { mutableSetOf() }.add(parts[1])
        }
        return result
    }
}
