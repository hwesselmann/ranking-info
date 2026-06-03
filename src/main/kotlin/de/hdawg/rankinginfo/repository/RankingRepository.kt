package de.hdawg.rankinginfo.repository

import de.hdawg.rankinginfo.domain.Ranking
import de.hdawg.rankinginfo.persistence.RankingEntity
import de.hdawg.rankinginfo.persistence.Rankings
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.between
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.countDistinct
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/** Repository for [de.hdawg.rankinginfo.domain.Ranking] backed by Exposed DAO/DSL. */
@Repository
@Transactional
class RankingRepository {
    fun findAll(): List<Ranking> = RankingEntity.all().map { it.toDomain() }

    fun count(): Long = Rankings.selectAll().count()

    fun deleteAll() {
        Rankings.deleteAll()
    }

    fun save(ranking: Ranking): Ranking =
        RankingEntity
            .new {
                dtbId = ranking.dtbId
                lastname = ranking.lastname
                firstname = ranking.firstname
                nationality = ranking.nationality
                ageGroup = ranking.ageGroup
                date = ranking.date
                rankingPosition = ranking.rankingPosition
                score = ranking.score
                club = ranking.club
                federation = ranking.federation
                ageGroupRanking = ranking.ageGroupRanking
                yobRanking = ranking.yobRanking
                yearEndRanking = ranking.yearEndRanking
                createdAt = ranking.createdAt
                updatedAt = ranking.updatedAt
            }.toDomain()

    fun saveAll(records: List<Ranking>) {
        if (records.isEmpty()) return
        Rankings.batchInsert(records) { r ->
            this[Rankings.dtbId] = r.dtbId
            this[Rankings.lastname] = r.lastname
            this[Rankings.firstname] = r.firstname
            this[Rankings.nationality] = r.nationality
            this[Rankings.ageGroup] = r.ageGroup
            this[Rankings.date] = r.date
            this[Rankings.rankingPosition] = r.rankingPosition
            this[Rankings.score] = r.score
            this[Rankings.club] = r.club
            this[Rankings.federation] = r.federation
            this[Rankings.ageGroupRanking] = r.ageGroupRanking
            this[Rankings.yobRanking] = r.yobRanking
            this[Rankings.yearEndRanking] = r.yearEndRanking
            this[Rankings.createdAt] = r.createdAt
            this[Rankings.updatedAt] = r.updatedAt
        }
    }

    fun findByDtbIdAndAgeGroupInOrderByDateDesc(
        dtbId: Int,
        ageGroups: List<String>,
    ): List<Ranking> =
        RankingEntity
            .find {
                (Rankings.dtbId eq dtbId) and (Rankings.ageGroup inList ageGroups)
            }.orderBy(Rankings.date to SortOrder.DESC)
            .map { it.toDomain() }

    fun findByDtbIdAndYobRankingFalseAndAgeGroupRankingFalseAndYearEndRankingFalseOrderByDateDescAgeGroupAsc(dtbId: Int): List<Ranking> =
        RankingEntity
            .find {
                (Rankings.dtbId eq dtbId) and
                    (Rankings.yobRanking eq false) and
                    (Rankings.ageGroupRanking eq false) and
                    (Rankings.yearEndRanking eq false)
            }.orderBy(Rankings.date to SortOrder.DESC, Rankings.ageGroup to SortOrder.ASC)
            .map { it.toDomain() }

    fun findByDtbIdAndYearEndRankingFalseOrderByDateAscAgeGroupAsc(dtbId: Int): List<Ranking> =
        RankingEntity
            .find {
                (Rankings.dtbId eq dtbId) and (Rankings.yearEndRanking eq false)
            }.orderBy(Rankings.date to SortOrder.ASC, Rankings.ageGroup to SortOrder.ASC)
            .map { it.toDomain() }

    /** Returns all distinct ranking dates before today, ordered descending. */
    @Cacheable("available_dates")
    fun findDistinctDatesDesc(): List<LocalDate> =
        Rankings
            .select(Rankings.date)
            .where { Rankings.date less LocalDate.now() }
            .withDistinct(true)
            .orderBy(Rankings.date to SortOrder.DESC)
            .map { it[Rankings.date] }

    /** Returns all distinct ranking dates including future-dated entries, ordered descending. */
    fun findAllDistinctDatesDesc(): List<LocalDate> =
        Rankings
            .select(Rankings.date)
            .withDistinct(true)
            .orderBy(Rankings.date to SortOrder.DESC)
            .map { it[Rankings.date] }

    /** Returns the number of distinct DTB IDs in the given range (used for player counts on the status page). */
    fun countDistinctDtbIdInRange(
        dtbIdStart: Int,
        dtbIdEnd: Int,
    ): Long {
        val countExpr = Rankings.dtbId.countDistinct()
        return Rankings
            .select(countExpr)
            .where { Rankings.dtbId.between(dtbIdStart, dtbIdEnd) }
            .single()[countExpr]
    }

    /** Returns all distinct federation names, ordered alphabetically. */
    fun findDistinctFederations(): List<String> =
        Rankings
            .select(Rankings.federation)
            .withDistinct(true)
            .orderBy(Rankings.federation to SortOrder.ASC)
            .map { it[Rankings.federation] }

    fun findForAgeRangeInPeriod(
        date: LocalDate,
        dtbIdStart: Int,
        dtbIdEnd: Int,
    ): List<Ranking> =
        RankingEntity
            .find {
                (Rankings.date eq date) and
                    (Rankings.dtbId.between(dtbIdStart, dtbIdEnd)) and
                    (Rankings.ageGroup eq "overall")
            }.orderBy(Rankings.rankingPosition to SortOrder.ASC, Rankings.dtbId to SortOrder.DESC)
            .map { it.toDomain() }

    fun findByLastnameLike(lastname: String): List<Ranking> =
        RankingEntity
            .find {
                Rankings.lastname.lowerCase() like "%${lastname.lowercase()}%"
            }.orderBy(
                Rankings.lastname to SortOrder.ASC,
                Rankings.dtbId to SortOrder.ASC,
                Rankings.date to SortOrder.DESC,
            ).map { it.toDomain() }

    fun findByLastnameAndYob(
        lastname: String,
        yobMaleStart: Int,
        yobMaleEnd: Int,
        yobFemaleStart: Int,
        yobFemaleEnd: Int,
    ): List<Ranking> =
        RankingEntity
            .find {
                (Rankings.lastname.lowerCase() like "%${lastname.lowercase()}%") and
                    (
                        (Rankings.dtbId.between(yobMaleStart, yobMaleEnd)) or
                            (Rankings.dtbId.between(yobFemaleStart, yobFemaleEnd))
                    )
            }.orderBy(
                Rankings.lastname to SortOrder.ASC,
                Rankings.dtbId to SortOrder.ASC,
                Rankings.date to SortOrder.DESC,
            ).map { it.toDomain() }

    fun findByYob(
        yobMaleStart: Int,
        yobMaleEnd: Int,
        yobFemaleStart: Int,
        yobFemaleEnd: Int,
    ): List<Ranking> =
        RankingEntity
            .find {
                (Rankings.dtbId.between(yobMaleStart, yobMaleEnd)) or
                    (Rankings.dtbId.between(yobFemaleStart, yobFemaleEnd))
            }.orderBy(
                Rankings.lastname to SortOrder.ASC,
                Rankings.dtbId to SortOrder.ASC,
                Rankings.date to SortOrder.DESC,
            ).map { it.toDomain() }

    fun findByDtbIdRange(
        dtbIdStart: Int,
        dtbIdEnd: Int,
    ): List<Ranking> =
        RankingEntity
            .find {
                Rankings.dtbId.between(dtbIdStart, dtbIdEnd)
            }.orderBy(
                Rankings.lastname to SortOrder.ASC,
                Rankings.dtbId to SortOrder.ASC,
                Rankings.date to SortOrder.DESC,
            ).map { it.toDomain() }

    fun countYouthByFederationAndAgeGroup(
        date: LocalDate,
        dtbIdStart: Int,
        dtbIdEnd: Int,
    ): List<Array<Any>> {
        val countExpr = Rankings.id.count()
        return Rankings
            .select(Rankings.federation, Rankings.ageGroup, countExpr)
            .where {
                (Rankings.date eq date) and
                    (Rankings.yobRanking eq false) and
                    (Rankings.ageGroupRanking eq true) and
                    (Rankings.yearEndRanking eq false) and
                    (Rankings.dtbId.between(dtbIdStart, dtbIdEnd))
            }.groupBy(Rankings.federation, Rankings.ageGroup)
            .map { arrayOf<Any>(it[Rankings.federation], it[Rankings.ageGroup], it[countExpr]) }
    }

    fun countAdultByFederation(
        date: LocalDate,
        ageGroup: String,
    ): List<Array<Any>> {
        val countExpr = Rankings.id.count()
        return Rankings
            .select(Rankings.federation, countExpr)
            .where {
                (Rankings.date eq date) and (Rankings.ageGroup eq ageGroup)
            }.groupBy(Rankings.federation)
            .map { arrayOf<Any>(it[Rankings.federation], it[countExpr]) }
    }

    fun findByDateAndAgeGroupAndClubContaining(
        date: LocalDate,
        ageGroup: String,
        club: String,
    ): List<Ranking> =
        RankingEntity
            .find {
                (Rankings.date eq date) and
                    (Rankings.ageGroup eq ageGroup) and
                    (Rankings.club.lowerCase() like "%${club.lowercase()}%")
            }.orderBy(Rankings.rankingPosition to SortOrder.ASC)
            .map { it.toDomain() }

    fun findAgeGroupRankingsByDateAndDtbIds(
        date: LocalDate,
        dtbIds: Collection<Int>,
    ): List<Ranking> =
        RankingEntity
            .find {
                (Rankings.date eq date) and
                    (Rankings.yobRanking eq false) and
                    (Rankings.ageGroupRanking eq true) and
                    (Rankings.yearEndRanking eq false) and
                    (Rankings.dtbId inList dtbIds.toList())
            }.map { it.toDomain() }

    /** Returns a paginated page of [Ranking] records matching [filter], ordered by ranking position. */
    fun findFiltered(
        filter: RankingQueryFilter,
        page: Int,
        perPage: Int,
    ): Page<Ranking> {
        val query =
            Rankings.selectAll().where { buildWhere(filter) }.orderBy(Rankings.rankingPosition to SortOrder.ASC)
        val total = query.count()
        val content =
            RankingEntity
                .wrapRows(query.limit(perPage).offset(((page - 1) * perPage).toLong()))
                .map { it.toDomain() }
        return PageImpl(content, PageRequest.of(page - 1, perPage), total)
    }

    /** Returns all [Ranking] records matching [filter] without pagination. */
    fun findFiltered(filter: RankingQueryFilter): List<Ranking> =
        RankingEntity.wrapRows(Rankings.selectAll().where { buildWhere(filter) }).map { it.toDomain() }

    private fun buildWhere(filter: RankingQueryFilter) =
        with(Rankings) {
            var pred =
                (date eq filter.date) and
                    (ageGroup eq filter.ageGroup) and
                    (dtbId.between(filter.dtbIdStart, filter.dtbIdEnd)) and
                    (yobRanking eq filter.yobRanking) and
                    (ageGroupRanking eq filter.ageGroupRanking) and
                    (yearEndRanking eq filter.yearEndRanking)

            filter.federation?.let { pred = pred and (federation eq it) }
            filter.club?.let { pred = pred and (club.lowerCase() like "%${it.lowercase()}%") }
            filter.dtbIds?.let { pred = pred and (dtbId inList it) }

            pred
        }
}
