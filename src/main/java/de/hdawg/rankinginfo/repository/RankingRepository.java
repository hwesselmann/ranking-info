package de.hdawg.rankinginfo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.persistence.RankingEntity;

@Repository
public class RankingRepository {

  private final RankingEntityRepository jpa;

  public RankingRepository(RankingEntityRepository jpa) {
    this.jpa = jpa;
  }

  public List<Ranking> findAll() {
    return jpa.findAll().stream().map(RankingEntity::toDomain).toList();
  }

  public long count() {
    return jpa.count();
  }

  public void deleteAll() {
    jpa.deleteAll();
  }

  public Ranking save(Ranking ranking) {
    return jpa.save(RankingEntity.fromDomain(ranking)).toDomain();
  }

  public void saveAll(List<Ranking> records) {
    jpa.saveAll(records.stream().map(RankingEntity::fromDomain).toList());
  }

  public List<Ranking> findByDtbIdAndAgeGroupInOrderByDateDesc(int dtbId, List<String> ageGroups) {
    return jpa.findByDtbIdAndAgeGroupInOrderByDateDesc(dtbId, ageGroups).stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public List<Ranking> findByDtbIdAndYobRankingFalseAndAgeGroupRankingFalseAndYearEndRankingFalseOrderByDateDescAgeGroupAsc(
      int dtbId) {
    return jpa.findOverallByDtbId(dtbId).stream().map(RankingEntity::toDomain).toList();
  }

  public List<Ranking> findByDtbIdAndYearEndRankingFalseOrderByDateAscAgeGroupAsc(int dtbId) {
    return jpa.findNonYearEndByDtbId(dtbId).stream().map(RankingEntity::toDomain).toList();
  }

  @Cacheable("available_dates")
  public List<LocalDate> findDistinctDatesDesc() {
    return jpa.queryDistinctDatesDesc(LocalDate.now());
  }

  public List<LocalDate> findAllDistinctDatesDesc() {
    return jpa.queryAllDistinctDatesDesc();
  }

  public long countDistinctDtbIdInRange(int dtbIdStart, int dtbIdEnd) {
    return jpa.countDistinctDtbIdInRange(dtbIdStart, dtbIdEnd);
  }

  @Cacheable("federations")
  public List<String> findDistinctFederations() {
    return jpa.queryDistinctFederations();
  }

  public List<Ranking> findForAgeRangeInPeriod(LocalDate date, int dtbIdStart, int dtbIdEnd) {
    return jpa.findForAgeRangeInPeriod(date, dtbIdStart, dtbIdEnd).stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public List<Ranking> findByLastnameLike(String lastname) {
    return jpa.findByLastnameLikeIgnoreCase("%" + lastname.toLowerCase() + "%").stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public List<Ranking> findByLastnameAndYob(
      String lastname,
      int yobMaleStart,
      int yobMaleEnd,
      int yobFemaleStart,
      int yobFemaleEnd) {
    return jpa
        .findByLastnameAndYob(
            "%" + lastname.toLowerCase() + "%",
            yobMaleStart,
            yobMaleEnd,
            yobFemaleStart,
            yobFemaleEnd)
        .stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public List<Ranking> findByYob(
      int yobMaleStart, int yobMaleEnd, int yobFemaleStart, int yobFemaleEnd) {
    return jpa.findByYob(yobMaleStart, yobMaleEnd, yobFemaleStart, yobFemaleEnd).stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public List<Ranking> findByDtbIdRange(int dtbIdStart, int dtbIdEnd) {
    return jpa.findByDtbIdRange(dtbIdStart, dtbIdEnd).stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public List<Object[]> countYouthByFederationAndAgeGroup(
      LocalDate date, int dtbIdStart, int dtbIdEnd) {
    return jpa.countYouthByFederationAndAgeGroup(date, dtbIdStart, dtbIdEnd);
  }

  public List<Object[]> countAdultByFederation(LocalDate date, String ageGroup) {
    return jpa.countAdultByFederation(date, ageGroup);
  }

  public List<Ranking> findByDateAndAgeGroupAndClubContaining(
      LocalDate date, String ageGroup, String club) {
    return jpa.findByDateAgeGroupAndClub(date, ageGroup, "%" + club.toLowerCase() + "%").stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public List<Ranking> findAgeGroupRankingsByDateAndDtbIds(LocalDate date, List<Integer> dtbIds) {
    return jpa.findAgeGroupRankingsByDateAndDtbIds(date, dtbIds).stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public Page<Ranking> findFiltered(RankingQueryFilter filter, int page, int perPage) {
    var spec = RankingSpecifications.matchesFilter(filter);
    var pageRequest =
        PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.ASC, "rankingPosition"));
    var entityPage = jpa.findAll(spec, pageRequest);
    var domainContent = entityPage.getContent().stream().map(RankingEntity::toDomain).toList();
    return new PageImpl<>(domainContent, entityPage.getPageable(), entityPage.getTotalElements());
  }

  public List<Ranking> findFiltered(RankingQueryFilter filter) {
    return jpa.findAll(RankingSpecifications.matchesFilter(filter)).stream()
        .map(RankingEntity::toDomain)
        .toList();
  }
}
