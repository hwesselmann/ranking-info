package de.hdawg.rankinginfo.domain

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Represents a single ranking entry for a player in a specific age group and period.
 *
 * Each record captures the player's position, score, and classification flags
 * (year-of-birth ranking, age-group ranking, year-end ranking) for a given quarter.
 */
data class Ranking(
    val id: Long = 0,
    val dtbId: Int = 0,
    val lastname: String = "",
    val firstname: String = "",
    val nationality: String = "",
    val ageGroup: String = "",
    val date: LocalDate = LocalDate.now(),
    val rankingPosition: Int = 0,
    val score: String = "",
    val club: String = "",
    val federation: String = "",
    val ageGroupRanking: Boolean = false,
    val yobRanking: Boolean = false,
    val yearEndRanking: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
