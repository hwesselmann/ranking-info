package de.hdawg.rankinginfo.persistence

import de.hdawg.rankinginfo.domain.Ranking
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.datetime

object Rankings : LongIdTable("rankings") {
    val dtbId = integer("dtb_id")
    val lastname = varchar("lastname", 50)
    val firstname = varchar("firstname", 50)
    val nationality = varchar("nationality", 3)
    val ageGroup = varchar("age_group", 20)
    val date = date("date")
    val rankingPosition = integer("ranking_position")
    val score = varchar("score", 20)
    val club = varchar("club", 255)
    val federation = varchar("federation", 50)
    val ageGroupRanking = bool("age_group_ranking")
    val yobRanking = bool("yob_ranking")
    val yearEndRanking = bool("year_end_ranking")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

class RankingEntity(
    id: EntityID<Long>,
) : LongEntity(id) {
    companion object : LongEntityClass<RankingEntity>(Rankings)

    var dtbId by Rankings.dtbId
    var lastname by Rankings.lastname
    var firstname by Rankings.firstname
    var nationality by Rankings.nationality
    var ageGroup by Rankings.ageGroup
    var date by Rankings.date
    var rankingPosition by Rankings.rankingPosition
    var score by Rankings.score
    var club by Rankings.club
    var federation by Rankings.federation
    var ageGroupRanking by Rankings.ageGroupRanking
    var yobRanking by Rankings.yobRanking
    var yearEndRanking by Rankings.yearEndRanking
    var createdAt by Rankings.createdAt
    var updatedAt by Rankings.updatedAt

    fun toDomain() =
        Ranking(
            id.value,
            dtbId,
            lastname,
            firstname,
            nationality,
            ageGroup,
            date,
            rankingPosition,
            score,
            club,
            federation,
            ageGroupRanking,
            yobRanking,
            yearEndRanking,
            createdAt,
            updatedAt,
        )
}
