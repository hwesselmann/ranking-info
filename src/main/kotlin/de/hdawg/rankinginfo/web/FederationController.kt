package de.hdawg.rankinginfo.web

import de.hdawg.rankinginfo.repository.RankingRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Web controller for the federation overview page at `/federations`.
 *
 * Shows player counts per federation grouped by age group for the most recent ranking quarter.
 */
@Controller
@RequestMapping("/federations")
class FederationController(
    private val rankingRepository: RankingRepository,
) {
    companion object {
        private val FEDERATION_NAMES =
            mapOf(
                "BAD" to "Baden",
                "BB" to "Berlin-Brandenburg",
                "BTV" to "Bayern",
                "HAM" to "Hamburg",
                "HTV" to "Hessen",
                "RPF" to "Rheinland-Pfalz",
                "SLH" to "Schleswig-Holstein",
                "STB" to "Saarland",
                "STV" to "Sachsen",
                "TMV" to "Mecklenburg-Vorpommern",
                "TNB" to "Niedersachsen-Bremen",
                "TSA" to "Sachsen-Anhalt",
                "TTV" to "Thüringen",
                "TVM" to "Mittelrhein",
                "TVN" to "Niederrhein",
                "WTB" to "Württemberg",
                "WTV" to "Westfalen",
            )
    }

    /**
     * Renders the federation overview with player counts per federation and age group.
     *
     * Youth counts are queried separately per gender; adult counts cover m00 and w00.
     */
    @GetMapping
    fun index(model: Model): String {
        val quarter = rankingRepository.findDistinctDatesDesc().firstOrNull()
        val federations = mutableMapOf<String, MutableMap<String, Int>>()

        if (quarter != null) {
            // Youth counts grouped by federation and age group (one query per gender)
            for (gender in listOf("m", "w")) {
                val dtbIdStart = if (gender == "m") 10_000_000 else 20_000_000
                rankingRepository
                    .countYouthByFederationAndAgeGroup(quarter, dtbIdStart, dtbIdStart + 9_999_999)
                    .forEach { row ->
                        val fed = FEDERATION_NAMES[row[0] as String] ?: (row[0] as String)
                        val ag = row[1] as String
                        val count = (row[2] as Long).toInt()
                        federations.getOrPut(fed) { mutableMapOf() }["${ag}$gender"] = count
                    }
            }
            // Adult counts (m00, w00)
            for (ag in listOf("m00", "w00")) {
                rankingRepository
                    .countAdultByFederation(quarter, ag)
                    .forEach { row ->
                        val fed = FEDERATION_NAMES[row[0] as String] ?: (row[0] as String)
                        val count = (row[1] as Long).toInt()
                        federations[fed]?.set(ag, count)
                    }
            }
        }
        model.addAttribute("federations", federations)
        return "federations/index"
    }
}
