package de.hdawg.rankinginfo.repository

import de.hdawg.rankinginfo.domain.ImportHistory
import de.hdawg.rankinginfo.persistence.ImportHistories
import de.hdawg.rankinginfo.persistence.ImportHistoryEntity
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.select
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

/** Repository for [de.hdawg.rankinginfo.domain.ImportHistory] backed by Exposed DAO. */
@Repository
@Transactional
class ImportHistoryRepository {
    fun findAll(): List<ImportHistory> = ImportHistoryEntity.all().map { it.toDomain() }

    fun deleteAll() {
        ImportHistories.deleteAll()
    }

    fun save(importHistory: ImportHistory): ImportHistory =
        ImportHistoryEntity
            .new {
                filename = importHistory.filename
                category = importHistory.category
                period = importHistory.period
                importedAt = importHistory.importedAt
                createdAt = importHistory.createdAt
                updatedAt = importHistory.updatedAt
            }.toDomain()

    /** Returns true if a record for the given [category] and [period] already exists. */
    fun existsByCategoryAndPeriod(
        category: String,
        period: LocalDate,
    ): Boolean =
        ImportHistoryEntity
            .find {
                (ImportHistories.category eq category) and (ImportHistories.period eq period)
            }.count() > 0

    /** Returns the maximum [ImportHistory.importedAt] value, or null if no imports exist. */
    fun findMaxImportedAt(): LocalDateTime? {
        val maxExpr = ImportHistories.importedAt.max()
        return ImportHistories.select(maxExpr).map { it[maxExpr] }.firstOrNull()
    }
}
