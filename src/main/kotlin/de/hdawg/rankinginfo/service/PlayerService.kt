package de.hdawg.rankinginfo.service

import de.hdawg.rankinginfo.domain.Club
import de.hdawg.rankinginfo.domain.Player
import de.hdawg.rankinginfo.domain.Ranking
import de.hdawg.rankinginfo.repository.RankingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for player-oriented queries: profile loading and player search.
 *
 * DTB IDs encode gender and year of birth: male IDs start with 1, female with 2.
 * The remaining digits encode year-of-birth class and a sequential number.
 */
@Service
@Transactional(readOnly = true)
class PlayerService(
    private val rankingRepository: RankingRepository,
) {
    /**
     * Builds a full [Player] profile from the ranking history for the given DTB ID.
     *
     * Uses the most recent overall/adult ranking entry as the current club and federation.
     * The club history is deduplicated and sorted alphabetically.
     *
     * @throws NoSuchElementException if no rankings exist for the given [dtbId]
     */
    fun loadPlayerProfile(dtbId: Int): Player {
        val allRankings =
            rankingRepository.findByDtbIdAndAgeGroupInOrderByDateDesc(
                dtbId,
                listOf("overall", "m00", "w00"),
            )
        val current = allRankings.first()
        val clubs =
            allRankings
                .map { Club(it.club, it.federation) }
                .distinctBy { it.name }
                .sortedBy { it.name }
        return Player(
            dtbId,
            current.lastname,
            current.firstname,
            current.nationality,
            current.club,
            current.federation,
            clubs,
        )
    }

    /** Finds players by last name (case-insensitive substring match), deduplicated by DTB ID. */
    fun findPlayersByLastname(lastname: String): List<Ranking> = deduplicateByDtbId(rankingRepository.findByLastnameLike(lastname))

    /**
     * Finds players by last name and year of birth, deduplicated by DTB ID.
     *
     * @param yobMale encoded YOB prefix for male players
     * @param yobFemale encoded YOB prefix for female players
     */
    fun findPlayersByLastnameAndYob(
        lastname: String,
        yobMale: Int,
        yobFemale: Int,
    ): List<Ranking> =
        deduplicateByDtbId(
            rankingRepository.findByLastnameAndYob(
                lastname,
                yobMale * 100_000,
                yobMale * 100_000 + 99_999,
                yobFemale * 100_000,
                yobFemale * 100_000 + 99_999,
            ),
        )

    /** Finds all players in the given year-of-birth class, deduplicated by DTB ID. */
    fun findPlayersByYob(
        yobMale: Int,
        yobFemale: Int,
    ): List<Ranking> =
        deduplicateByDtbId(
            rankingRepository.findByYob(
                yobMale * 100_000,
                yobMale * 100_000 + 99_999,
                yobFemale * 100_000,
                yobFemale * 100_000 + 99_999,
            ),
        )

    /** Finds players whose DTB ID falls within the given inclusive range, deduplicated by DTB ID. */
    fun findPlayersByDtbIdRange(
        dtbIdStart: Int,
        dtbIdEnd: Int,
    ): List<Ranking> = deduplicateByDtbId(rankingRepository.findByDtbIdRange(dtbIdStart, dtbIdEnd))

    private fun deduplicateByDtbId(rankings: List<Ranking>): List<Ranking> {
        val seen = mutableSetOf<Int>()
        return rankings.filter { seen.add(it.dtbId) }
    }
}
