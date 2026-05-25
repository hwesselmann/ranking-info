package de.hdawg.rankinginfo.domain

/**
 * Represents a tennis player with their current club affiliation and full club history.
 *
 * @property dtbId unique DTB identifier encoding gender and year of birth
 * @property clubs all clubs the player has appeared under across all available ranking periods
 */
data class Player(
    val dtbId: Int,
    val lastname: String,
    val firstname: String,
    val nationality: String,
    val club: String,
    val federation: String,
    val clubs: List<Club> = emptyList(),
)
