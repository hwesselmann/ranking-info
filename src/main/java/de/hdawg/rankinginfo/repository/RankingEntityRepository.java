package de.hdawg.rankinginfo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.hdawg.rankinginfo.persistence.RankingEntity;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
interface RankingEntityRepository
    extends JpaRepository<RankingEntity, Long>, JpaSpecificationExecutor<RankingEntity> {

  List<RankingEntity> findByDtbIdAndAgeGroupInOrderByDateDesc(
      Integer dtbId, List<String> ageGroups);

  @Query(
      "SELECT r FROM RankingEntity r WHERE r.dtbId = :dtbId "
          + "AND r.yobRanking = false AND r.ageGroupRanking = false AND r.yearEndRanking = false "
          + "ORDER BY r.date DESC, r.ageGroup ASC")
  List<RankingEntity> findOverallByDtbId(@Param("dtbId") Integer dtbId);

  @Query(
      "SELECT r FROM RankingEntity r WHERE r.dtbId = :dtbId AND r.yearEndRanking = false "
          + "ORDER BY r.date ASC, r.ageGroup ASC")
  List<RankingEntity> findNonYearEndByDtbId(@Param("dtbId") Integer dtbId);

  @Query(
      "SELECT DISTINCT r.date FROM RankingEntity r WHERE r.date < :today ORDER BY r.date DESC")
  List<LocalDate> queryDistinctDatesDesc(@Param("today") LocalDate today);

  @Query("SELECT DISTINCT r.date FROM RankingEntity r ORDER BY r.date DESC")
  List<LocalDate> queryAllDistinctDatesDesc();

  @Query(
      "SELECT COUNT(DISTINCT r.dtbId) FROM RankingEntity r WHERE r.dtbId BETWEEN :start AND :end")
  long countDistinctDtbIdInRange(@Param("start") int start, @Param("end") int end);

  @Query("SELECT DISTINCT r.federation FROM RankingEntity r ORDER BY r.federation ASC")
  List<String> queryDistinctFederations();

  @Query(
      "SELECT r FROM RankingEntity r WHERE r.date = :date "
          + "AND r.dtbId BETWEEN :start AND :end AND r.ageGroup = 'overall' "
          + "ORDER BY r.rankingPosition ASC, r.dtbId DESC")
  List<RankingEntity> findForAgeRangeInPeriod(
      @Param("date") LocalDate date, @Param("start") int start, @Param("end") int end);

  @Query(
      "SELECT r FROM RankingEntity r WHERE LOWER(r.lastname) LIKE :pattern "
          + "ORDER BY r.lastname ASC, r.dtbId ASC, r.date DESC")
  List<RankingEntity> findByLastnameLikeIgnoreCase(@Param("pattern") String pattern);

  @Query(
      "SELECT r FROM RankingEntity r WHERE LOWER(r.lastname) LIKE :pattern "
          + "AND (r.dtbId BETWEEN :maleStart AND :maleEnd "
          + "OR r.dtbId BETWEEN :femaleStart AND :femaleEnd) "
          + "ORDER BY r.lastname ASC, r.dtbId ASC, r.date DESC")
  List<RankingEntity> findByLastnameAndYob(
      @Param("pattern") String pattern,
      @Param("maleStart") int maleStart,
      @Param("maleEnd") int maleEnd,
      @Param("femaleStart") int femaleStart,
      @Param("femaleEnd") int femaleEnd);

  @Query(
      "SELECT r FROM RankingEntity r "
          + "WHERE (r.dtbId BETWEEN :maleStart AND :maleEnd "
          + "OR r.dtbId BETWEEN :femaleStart AND :femaleEnd) "
          + "ORDER BY r.lastname ASC, r.dtbId ASC, r.date DESC")
  List<RankingEntity> findByYob(
      @Param("maleStart") int maleStart,
      @Param("maleEnd") int maleEnd,
      @Param("femaleStart") int femaleStart,
      @Param("femaleEnd") int femaleEnd);

  @Query(
      "SELECT r FROM RankingEntity r WHERE r.dtbId BETWEEN :start AND :end "
          + "ORDER BY r.lastname ASC, r.dtbId ASC, r.date DESC")
  List<RankingEntity> findByDtbIdRange(@Param("start") int start, @Param("end") int end);

  @Query(
      "SELECT r.federation, r.ageGroup, COUNT(r.id) FROM RankingEntity r "
          + "WHERE r.date = :date AND r.yobRanking = false AND r.ageGroupRanking = true "
          + "AND r.yearEndRanking = false AND r.dtbId BETWEEN :start AND :end "
          + "GROUP BY r.federation, r.ageGroup")
  List<Object[]> countYouthByFederationAndAgeGroup(
      @Param("date") LocalDate date, @Param("start") int start, @Param("end") int end);

  @Query(
      "SELECT r.federation, COUNT(r.id) FROM RankingEntity r "
          + "WHERE r.date = :date AND r.ageGroup = :ageGroup "
          + "GROUP BY r.federation")
  List<Object[]> countAdultByFederation(
      @Param("date") LocalDate date, @Param("ageGroup") String ageGroup);

  @Query(
      "SELECT r FROM RankingEntity r WHERE r.date = :date AND r.ageGroup = :ageGroup "
          + "AND LOWER(r.club) LIKE :clubPattern ORDER BY r.rankingPosition ASC")
  List<RankingEntity> findByDateAgeGroupAndClub(
      @Param("date") LocalDate date,
      @Param("ageGroup") String ageGroup,
      @Param("clubPattern") String clubPattern);

  @Query(
      "SELECT r FROM RankingEntity r WHERE r.date = :date "
          + "AND r.yobRanking = false AND r.ageGroupRanking = true AND r.yearEndRanking = false "
          + "AND r.dtbId IN :dtbIds")
  List<RankingEntity> findAgeGroupRankingsByDateAndDtbIds(
      @Param("date") LocalDate date, @Param("dtbIds") List<Integer> dtbIds);
}
