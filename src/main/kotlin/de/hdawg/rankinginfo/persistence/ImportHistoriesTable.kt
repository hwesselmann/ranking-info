package de.hdawg.rankinginfo.persistence

import de.hdawg.rankinginfo.domain.ImportHistory
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.datetime

object ImportHistories : LongIdTable("import_histories") {
    val filename = varchar("filename", 255)
    val category = varchar("category", 50)
    val period = date("period")
    val importedAt = datetime("imported_at")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class ImportHistoryEntity(
    id: EntityID<Long>,
) : LongEntity(id) {
    companion object : LongEntityClass<ImportHistoryEntity>(ImportHistories)

    var filename by ImportHistories.filename
    var category by ImportHistories.category
    var period by ImportHistories.period
    var importedAt by ImportHistories.importedAt
    var createdAt by ImportHistories.createdAt
    var updatedAt by ImportHistories.updatedAt

    fun toDomain() = ImportHistory(id.value, filename, category, period, importedAt, createdAt, updatedAt)
}
