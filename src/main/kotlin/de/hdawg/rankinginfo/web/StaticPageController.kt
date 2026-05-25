package de.hdawg.rankinginfo.web

import de.hdawg.rankinginfo.repository.ImportHistoryRepository
import de.hdawg.rankinginfo.repository.RankingRepository
import de.hdawg.rankinginfo.service.RankingService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.time.format.DateTimeFormatter

/**
 * Web controller for static and semi-static pages (home, help, about, privacy, status).
 *
 * Imprint data for the privacy page is injected from application properties (`imprint.*`).
 * The status page shows live statistics (player counts, ranking count, last import time).
 */
@Controller
class StaticPageController(
    private val rankingRepository: RankingRepository,
    private val importHistoryRepository: ImportHistoryRepository,
    private val rankingService: RankingService,
    @Value("\${imprint.domain:localhost}") private val domain: String,
    @Value("\${imprint.name:}") private val imprintName: String,
    @Value("\${imprint.street:}") private val imprintStreet: String,
    @Value("\${imprint.zipcode:}") private val imprintZipcode: String,
    @Value("\${imprint.city:}") private val imprintCity: String,
    @Value("\${imprint.phone:}") private val imprintPhone: String,
    @Value("\${imprint.mail:}") private val imprintMail: String,
) {
    companion object {
        private const val MALE_DTB_ID_START = 10_000_000
        private const val MALE_DTB_ID_END = 19_999_999
        private const val FEMALE_DTB_ID_START = 20_000_000
        private const val FEMALE_DTB_ID_END = 29_999_999
    }

    private val dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    @GetMapping("/")
    fun home(model: Model): String {
        val firstDate = rankingRepository.findAllDistinctDatesDesc().lastOrNull()
        model.addAttribute("start", firstDate?.minusDays(1)?.format(dtf) ?: "xx.xx.xxxx")
        return "static_pages/home"
    }

    @GetMapping("/help")
    fun help(model: Model): String = "static_pages/help"

    @GetMapping("/about")
    fun about(model: Model): String {
        addImprintData(model)
        return "static_pages/about"
    }

    @GetMapping("/privacy")
    fun privacy(model: Model): String {
        addImprintData(model)
        return "static_pages/privacy"
    }

    @GetMapping("/status")
    fun status(model: Model): String {
        model.addAttribute(
            "playerMaleCount",
            rankingRepository.countDistinctDtbIdInRange(MALE_DTB_ID_START, MALE_DTB_ID_END),
        )
        model.addAttribute(
            "playerFemaleCount",
            rankingRepository.countDistinctDtbIdInRange(FEMALE_DTB_ID_START, FEMALE_DTB_ID_END),
        )
        model.addAttribute("rankingCount", rankingRepository.count())
        model.addAttribute(
            "dateLastUpdated",
            importHistoryRepository
                .findMaxImportedAt()
                ?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) ?: "",
        )
        val quartersByYear =
            rankingRepository
                .findDistinctDatesDesc()
                .filter { !(it.monthValue == 12 && it.dayOfMonth == 31) }
                .groupBy { it.year }
                .mapValues { (_, dates) -> dates.map { it.monthValue }.toSet() }
                .entries
                .sortedByDescending { it.key }
                .associate { it.key to it.value }
        model.addAttribute("quartersByYear", quartersByYear)
        return "static_pages/status"
    }

    private fun addImprintData(model: Model) {
        model.addAttribute("imprintDomain", domain)
        model.addAttribute("imprintName", imprintName)
        model.addAttribute("imprintStreet", imprintStreet)
        model.addAttribute("imprintZipcode", imprintZipcode)
        model.addAttribute("imprintCity", imprintCity)
        model.addAttribute("imprintPhone", imprintPhone)
        model.addAttribute("imprintMail", imprintMail)
    }
}
