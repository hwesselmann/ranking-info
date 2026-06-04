package de.hdawg.rankinginfo.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.persistence.RankingEntity;

@Repository
public class RankingRepository {

  private static final String BASE_FILTER_SQL =
      "SELECT * FROM rankings"
          + " WHERE date = :date AND age_group = :ageGroup"
          + " AND dtb_id BETWEEN :dtbIdStart AND :dtbIdEnd"
          + " AND yob_ranking = :yobRanking"
          + " AND age_group_ranking = :ageGroupRanking"
          + " AND year_end_ranking = :yearEndRanking";

  private static final String BASE_FILTER_COUNT_SQL =
      "SELECT COUNT(*) FROM rankings"
          + " WHERE date = :date AND age_group = :ageGroup"
          + " AND dtb_id BETWEEN :dtbIdStart AND :dtbIdEnd"
          + " AND yob_ranking = :yobRanking"
          + " AND age_group_ranking = :ageGroupRanking"
          + " AND year_end_ranking = :yearEndRanking";

  private static final RankingRowMapper ROW_MAPPER = new RankingRowMapper();

  private final RankingEntityRepository jdbc;
  private final NamedParameterJdbcTemplate namedJdbc;

  public RankingRepository(
      RankingEntityRepository jdbc, NamedParameterJdbcTemplate namedJdbc) {
    this.jdbc = jdbc;
    this.namedJdbc = namedJdbc;
  }

  public List<Ranking> findAll() {
    return jdbc.findAll().stream().map(RankingEntity::toDomain).toList();
  }

  public long count() {
    return jdbc.count();
  }

  public void deleteAll() {
    jdbc.deleteAll();
  }

  public Ranking save(Ranking ranking) {
    return jdbc.save(RankingEntity.fromDomain(ranking)).toDomain();
  }

  public void saveAll(List<Ranking> records) {
    jdbc.saveAll(records.stream().map(RankingEntity::fromDomain).toList());
  }

  public List<Ranking> findByDtbIdAndAgeGroupInOrderByDateDesc(int dtbId, List<String> ageGroups) {
    return jdbc.findByDtbIdAndAgeGroupInOrderByDateDesc(dtbId, ageGroups).stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public List<Ranking> findByDtbIdAndYobRankingFalseAndAgeGroupRankingFalseAndYearEndRankingFalseOrderByDateDescAgeGroupAsc(
      int dtbId) {
    return jdbc.findOverallByDtbId(dtbId).stream().map(RankingEntity::toDomain).toList();
  }

  public List<Ranking> findByDtbIdAndYearEndRankingFalseOrderByDateAscAgeGroupAsc(int dtbId) {
    return jdbc.findNonYearEndByDtbId(dtbId).stream().map(RankingEntity::toDomain).toList();
  }

  @Cacheable("available_dates")
  public List<LocalDate> findDistinctDatesDesc() {
    return jdbc.queryDistinctDatesDesc(LocalDate.now());
  }

  @Cacheable("player_counts")
  public long countDistinctDtbIdInRange(int dtbIdStart, int dtbIdEnd) {
    return jdbc.countDistinctDtbIdInRange(dtbIdStart, dtbIdEnd);
  }

  @Cacheable("federations")
  public List<String> findDistinctFederations() {
    return jdbc.queryDistinctFederations();
  }

  public List<Ranking> findForAgeRangeInPeriod(LocalDate date, int dtbIdStart, int dtbIdEnd) {
    return jdbc.findForAgeRangeInPeriod(date, dtbIdStart, dtbIdEnd).stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public List<Ranking> findByLastnameLike(String lastname) {
    return jdbc
        .findByLastnameLikeIgnoreCase("%" + lastname.toLowerCase(Locale.ROOT) + "%")
        .stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public List<Ranking> findByLastnameAndYob(
      String lastname,
      int yobMaleStart,
      int yobMaleEnd,
      int yobFemaleStart,
      int yobFemaleEnd) {
    return jdbc
        .findByLastnameAndYob(
            "%" + lastname.toLowerCase(Locale.ROOT) + "%",
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
    return jdbc.findByYob(yobMaleStart, yobMaleEnd, yobFemaleStart, yobFemaleEnd).stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public List<Ranking> findByDtbIdRange(int dtbIdStart, int dtbIdEnd) {
    return jdbc.findByDtbIdRange(dtbIdStart, dtbIdEnd).stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  @Cacheable("federation_stats")
  public List<FederationAgeGroupCount> countYouthByFederationAndAgeGroup(
      LocalDate date, int dtbIdStart, int dtbIdEnd) {
    return jdbc.countYouthByFederationAndAgeGroup(date, dtbIdStart, dtbIdEnd);
  }

  @Cacheable("federation_stats")
  public List<FederationCount> countAdultByFederation(LocalDate date, String ageGroup) {
    return jdbc.countAdultByFederation(date, ageGroup);
  }

  public List<Ranking> findByDateAndAgeGroupAndClubContaining(
      LocalDate date, String ageGroup, String club) {
    return jdbc
        .findByDateAgeGroupAndClub(date, ageGroup, "%" + club.toLowerCase(Locale.ROOT) + "%")
        .stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  public List<Ranking> findAgeGroupRankingsByDateAndDtbIds(LocalDate date, List<Integer> dtbIds) {
    return jdbc.findAgeGroupRankingsByDateAndDtbIds(date, dtbIds).stream()
        .map(RankingEntity::toDomain)
        .toList();
  }

  // buildOptionalClauses appends only static SQL fragments; all values are bound as named parameters
  @SuppressWarnings("java:S2077")
  public Page<Ranking> findFiltered(RankingQueryFilter filter, int page, int perPage) {
    var params = buildBaseParams(filter);
    var optionalSql = buildOptionalClauses(params, filter);

    var total =
        Objects.requireNonNullElse(
            namedJdbc.queryForObject(BASE_FILTER_COUNT_SQL + optionalSql, params, Long.class), 0L);

    params.addValue("limit", perPage).addValue("offset", (long) (page - 1) * perPage);
    var content =
        namedJdbc.query(
            BASE_FILTER_SQL + optionalSql + " ORDER BY ranking_position ASC LIMIT :limit OFFSET :offset",
            params,
            ROW_MAPPER);
    return new PageImpl<>(content, PageRequest.of(page - 1, perPage), total);
  }

  @SuppressWarnings("java:S2077")
  public List<Ranking> findFiltered(RankingQueryFilter filter) {
    var params = buildBaseParams(filter);
    var optionalSql = buildOptionalClauses(params, filter);
    return namedJdbc.query(
        BASE_FILTER_SQL + optionalSql + " ORDER BY ranking_position ASC", params, ROW_MAPPER);
  }

  private static MapSqlParameterSource buildBaseParams(RankingQueryFilter filter) {
    return new MapSqlParameterSource()
        .addValue("date", filter.date())
        .addValue("ageGroup", filter.ageGroup())
        .addValue("dtbIdStart", filter.dtbIdStart())
        .addValue("dtbIdEnd", filter.dtbIdEnd())
        .addValue("yobRanking", filter.yobRanking())
        .addValue("ageGroupRanking", filter.ageGroupRanking())
        .addValue("yearEndRanking", filter.yearEndRanking());
  }

  private static String buildOptionalClauses(
      MapSqlParameterSource params, RankingQueryFilter filter) {
    var sql = new StringBuilder();
    if (filter.federation() != null) {
      sql.append(" AND federation = :federation");
      params.addValue("federation", filter.federation());
    }
    if (filter.club() != null) {
      sql.append(" AND LOWER(club) LIKE :club");
      params.addValue("club", "%" + filter.club().toLowerCase(Locale.ROOT) + "%");
    }
    if (filter.dtbIds() != null && !filter.dtbIds().isEmpty()) {
      sql.append(" AND dtb_id IN (:dtbIds)");
      params.addValue("dtbIds", filter.dtbIds());
    }
    return sql.toString();
  }

  private static final class RankingRowMapper implements RowMapper<Ranking> {
    @Override
    public Ranking mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new Ranking(
          rs.getLong("id"),
          rs.getInt("dtb_id"),
          rs.getString("lastname"),
          rs.getString("firstname"),
          rs.getString("nationality"),
          rs.getString("age_group"),
          rs.getObject("date", LocalDate.class),
          rs.getInt("ranking_position"),
          rs.getString("score"),
          rs.getString("club"),
          rs.getString("federation"),
          rs.getBoolean("age_group_ranking"),
          rs.getBoolean("yob_ranking"),
          rs.getBoolean("year_end_ranking"),
          rs.getObject("created_at", LocalDateTime.class),
          rs.getObject("updated_at", LocalDateTime.class));
    }
  }
}
