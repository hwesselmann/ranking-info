package de.hdawg.rankinginfo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import de.hdawg.rankinginfo.persistence.RankingEntity;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
interface RankingEntityRepository extends ListCrudRepository<RankingEntity, Long> {

  @Query(
      "SELECT * FROM rankings WHERE dtb_id = :dtbId AND age_group IN (:ageGroups)"
          + " ORDER BY date DESC")
  List<RankingEntity> findByDtbIdAndAgeGroupInOrderByDateDesc(
      @Param("dtbId") Integer dtbId, @Param("ageGroups") List<String> ageGroups);

  @Query(
      "SELECT * FROM rankings WHERE dtb_id = :dtbId"
          + " AND yob_ranking = false AND age_group_ranking = false AND year_end_ranking = false"
          + " ORDER BY date DESC, age_group ASC")
  List<RankingEntity> findOverallByDtbId(@Param("dtbId") Integer dtbId);

  @Query(
      "SELECT * FROM rankings WHERE dtb_id = :dtbId AND year_end_ranking = false"
          + " ORDER BY date ASC, age_group ASC")
  List<RankingEntity> findNonYearEndByDtbId(@Param("dtbId") Integer dtbId);

  @Query("SELECT DISTINCT date FROM rankings WHERE date <= :today ORDER BY date DESC")
  List<LocalDate> queryDistinctDatesDesc(@Param("today") LocalDate today);

  @Query("SELECT COUNT(DISTINCT dtb_id) FROM rankings WHERE dtb_id BETWEEN :start AND :end")
  long countDistinctDtbIdInRange(@Param("start") int start, @Param("end") int end);

  @Query("SELECT DISTINCT federation FROM rankings ORDER BY federation ASC")
  List<String> queryDistinctFederations();

  @Query(
      "SELECT * FROM rankings WHERE date = :date"
          + " AND dtb_id BETWEEN :start AND :end AND age_group = 'overall'"
          + " ORDER BY ranking_position ASC, dtb_id DESC")
  List<RankingEntity> findForAgeRangeInPeriod(
      @Param("date") LocalDate date, @Param("start") int start, @Param("end") int end);

  @Query(
      "SELECT * FROM ("
          + "SELECT DISTINCT ON (dtb_id) * FROM rankings"
          + " WHERE LOWER(lastname) LIKE :pattern AND age_group IN ('overall', 'm00', 'w00')"
          + " ORDER BY dtb_id, date DESC"
          + ") sub ORDER BY lastname ASC")
  List<RankingEntity> findByLastnameLikeIgnoreCase(@Param("pattern") String pattern);

  @Query(
      "SELECT * FROM ("
          + "SELECT DISTINCT ON (dtb_id) * FROM rankings"
          + " WHERE LOWER(lastname) LIKE :pattern"
          + " AND (dtb_id BETWEEN :maleStart AND :maleEnd OR dtb_id BETWEEN :femaleStart AND :femaleEnd)"
          + " AND age_group IN ('overall', 'm00', 'w00')"
          + " ORDER BY dtb_id, date DESC"
          + ") sub ORDER BY lastname ASC")
  List<RankingEntity> findByLastnameAndYob(
      @Param("pattern") String pattern,
      @Param("maleStart") int maleStart,
      @Param("maleEnd") int maleEnd,
      @Param("femaleStart") int femaleStart,
      @Param("femaleEnd") int femaleEnd);

  @Query(
      "SELECT * FROM ("
          + "SELECT DISTINCT ON (dtb_id) * FROM rankings"
          + " WHERE (dtb_id BETWEEN :maleStart AND :maleEnd OR dtb_id BETWEEN :femaleStart AND :femaleEnd)"
          + " AND age_group IN ('overall', 'm00', 'w00')"
          + " ORDER BY dtb_id, date DESC"
          + ") sub ORDER BY lastname ASC")
  List<RankingEntity> findByYob(
      @Param("maleStart") int maleStart,
      @Param("maleEnd") int maleEnd,
      @Param("femaleStart") int femaleStart,
      @Param("femaleEnd") int femaleEnd);

  @Query(
      "SELECT * FROM ("
          + "SELECT DISTINCT ON (dtb_id) * FROM rankings"
          + " WHERE dtb_id BETWEEN :start AND :end AND age_group IN ('overall', 'm00', 'w00')"
          + " ORDER BY dtb_id, date DESC"
          + ") sub ORDER BY lastname ASC")
  List<RankingEntity> findByDtbIdRange(@Param("start") int start, @Param("end") int end);

  @Query(
      "SELECT federation, age_group, COUNT(id) AS count FROM rankings"
          + " WHERE date = :date AND yob_ranking = false AND age_group_ranking = true"
          + " AND year_end_ranking = false AND dtb_id BETWEEN :start AND :end"
          + " GROUP BY federation, age_group")
  List<FederationAgeGroupCount> countYouthByFederationAndAgeGroup(
      @Param("date") LocalDate date, @Param("start") int start, @Param("end") int end);

  @Query(
      "SELECT federation, COUNT(id) AS count FROM rankings"
          + " WHERE date = :date AND age_group = :ageGroup"
          + " GROUP BY federation")
  List<FederationCount> countAdultByFederation(
      @Param("date") LocalDate date, @Param("ageGroup") String ageGroup);

  @Query(
      "SELECT * FROM rankings WHERE date = :date AND age_group = :ageGroup"
          + " AND LOWER(club) LIKE :clubPattern ORDER BY ranking_position ASC")
  List<RankingEntity> findByDateAgeGroupAndClub(
      @Param("date") LocalDate date,
      @Param("ageGroup") String ageGroup,
      @Param("clubPattern") String clubPattern);

  @Query(
      "SELECT * FROM rankings WHERE date = :date AND age_group IN (:ageGroups)"
          + " AND LOWER(club) LIKE :clubPattern ORDER BY ranking_position ASC")
  List<RankingEntity> findByDateAgeGroupsAndClub(
      @Param("date") LocalDate date,
      @Param("ageGroups") List<String> ageGroups,
      @Param("clubPattern") String clubPattern);

  @Query(
      "SELECT * FROM rankings"
          + " WHERE date = :date AND yob_ranking = false AND age_group_ranking = true"
          + " AND year_end_ranking = false AND dtb_id IN (:dtbIds)")
  List<RankingEntity> findAgeGroupRankingsByDateAndDtbIds(
      @Param("date") LocalDate date, @Param("dtbIds") List<Integer> dtbIds);
}
