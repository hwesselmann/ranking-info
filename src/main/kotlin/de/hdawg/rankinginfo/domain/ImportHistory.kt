package de.hdawg.rankinginfo.domain

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Records a successful CSV import, preventing duplicate processing of the same file.
 *
 * Tracks which file was imported, the ranking category (e.g. Junioren, Damen),
 * and the ranking period it covers.
 */
data class ImportHistory(
    val id: Long = 0,
    val filename: String = "",
    val category: String = "",
    val period: LocalDate = LocalDate.now(),
    val importedAt: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
