package de.hdawg.rankinginfo.rest.repository;

import de.hdawg.rankinginfo.model.AgeGroup;
import de.hdawg.rankinginfo.model.Federation;
import de.hdawg.rankinginfo.model.Gender;
import de.hdawg.rankinginfo.model.Nationality;
import de.hdawg.rankinginfo.model.Ranking;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Data access layer for ranking data.
 */
@Component
public class RankingRepository {

  public static final String COLUMN_RANKINGPERIOD = "rankingperiod";
  public static final String COLUMN_DTBID = "dtbid";
  public static final String COLUMN_LASTNAME = "lastname";
  public static final String COLUMN_FIRSTNAME = "firstname";
  public static final String COLUMN_POINTS = "points";
  public static final String COLUMN_NATIONALITY = "nationality";
  public static final String COLUMN_FEDERATION = "federation";
  private final JdbcTemplate jdbcTemplate;

  public RankingRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * fetch rankings for listing response.
   *
   * @param quarter          ranking period
   * @param ageGroup         age group
   * @param gender           gender
   * @param isYobRanking     yob results only?
   * @param overallRanking   include all players?
   * @param endOfYearRanking end of year ranking?
   * @return list of rankings
   */
  public List<Ranking> getRankingsForListing(LocalDate quarter, AgeGroup ageGroup, Gender gender,
                                             boolean isYobRanking, boolean overallRanking,
                                             boolean endOfYearRanking) {
    String genderNumericalIdentifier = "1";
    if (Gender.girls == gender) {
      genderNumericalIdentifier = "2";
    }

    String sql = "SELECT * FROM ranking WHERE rankingperiod='" + quarter
        + "' AND agegroup='" + ageGroup.name().toUpperCase()
        + "' AND dtbid LIKE '" + genderNumericalIdentifier + "%'"
        + " AND yobrankings=" + isYobRanking
        + " AND overallranking=" + overallRanking
        + " AND endofyearranking=" + endOfYearRanking
        + " ORDER BY rankingposition ASC, points ASC";
    return jdbcTemplate.query(sql, (rs, rowNum) ->
        new Ranking(rs.getDate(COLUMN_RANKINGPERIOD).toLocalDate(), rs.getString(COLUMN_DTBID),
            rs.getString(COLUMN_LASTNAME), rs.getString(COLUMN_FIRSTNAME),
            rs.getString(COLUMN_POINTS), Nationality.valueOf(rs.getString(COLUMN_NATIONALITY)),
            Federation.valueOf(rs.getString(COLUMN_FEDERATION)), rs.getString("club"),
            rs.getString("agegroup"), rs.getInt("rankingposition"),
            isYobRanking, overallRanking, endOfYearRanking)
    );
  }

  /**
   * fetch rankings that match the given criteria.
   *
   * @param dtbId unique id (or partial id)
   * @param name  lastname (or partial)
   * @param yob   year of birth
   * @return list of rankings or empty list.
   */
  public List<Ranking> findPlayers(String dtbId, String name, String yob) {
    String sql =
        "SELECT DISTINCT (DTBID), DTBID, FIRSTNAME, LASTNAME, NATIONALITY, CLUB, FEDERATION, RANKINGPERIOD "
            + "FROM RANKING";
    if (dtbId != null && !dtbId.isEmpty()) {
      sql += " WHERE DTBID LIKE '" + dtbId + "%'";
    }
    if (name != null && !name.isEmpty()) {
      if (dtbId != null && !dtbId.isEmpty()) {
        sql += " AND";
      } else {
        sql += " WHERE";
      }
      sql += " lastname LIKE '" + name + "%'";
    }
    if (yob != null && !yob.isEmpty()) {
      if ((dtbId != null && !dtbId.isEmpty()) || (name != null && !name.isEmpty())) {
        sql += " AND";
      } else {
        sql += " WHERE";
      }
      sql += " (dtbid LIKE '1" + yob.substring(2, 4) + "%'"
          + " OR dtbid LIKE '2" + yob.substring(2, 4) + "%')";
    }
    sql += " ORDER BY DTBID, RANKINGPERIOD DESC";
    return jdbcTemplate.query(sql, (rs, rownum) ->
        new Ranking(rs.getDate(COLUMN_RANKINGPERIOD).toLocalDate(), rs.getString(COLUMN_DTBID),
            rs.getString(COLUMN_LASTNAME), rs.getString(COLUMN_FIRSTNAME),
            "", Nationality.valueOf(rs.getString(COLUMN_NATIONALITY)),
            Federation.valueOf(rs.getString(COLUMN_FEDERATION)), rs.getString("club"),
            "", 0, false, false, false)
    );
  }

  /**
   * get all rankings for a player with a given dtb id.
   *
   * @param dtbId unique id
   * @return list of rankings
   */
  public List<Ranking> getRankingsForPlayer(final String dtbId) {
    final String sql =
        "SELECT * FROM RANKING WHERE DTBID=? ORDER BY RANKINGPERIOD DESC, AGEGROUP ASC";
    return jdbcTemplate.query(sql, ps -> ps.setString(1, dtbId),
        (rs, rownum) -> new Ranking(rs.getDate(COLUMN_RANKINGPERIOD).toLocalDate(),
            rs.getString(COLUMN_DTBID),
            rs.getString(COLUMN_LASTNAME), rs.getString(COLUMN_FIRSTNAME),
            rs.getString(COLUMN_POINTS), Nationality.valueOf(rs.getString(COLUMN_NATIONALITY)),
            Federation.valueOf(rs.getString(COLUMN_FEDERATION)), rs.getString("club"),
            rs.getString("agegroup"), rs.getInt("rankingposition"),
            rs.getBoolean("yobrankings"), rs.getBoolean("overallranking"),
            rs.getBoolean("endofyearranking")));
  }

  /**
   * retrieve all available ranking periods.
   *
   * @return list of ranking periods
   */
  public List<LocalDate> getAvailableRankingPeriods() {
    final String sql = "SELECT DISTINCT(RANKINGPERIOD) FROM RANKING ORDER BY RANKINGPERIOD ASC";
    return jdbcTemplate.query(sql, (rs, rownum) -> rs.getDate(1).toLocalDate());
  }

  /**
   * Retrieve the most recent ranking period in the system.
   *
   * @return ranking period or null
   */
  public LocalDate getMostRecentRankingPeriod() {
    return getAvailableRankingPeriods().stream().max(Comparator.naturalOrder()).orElse(null);
  }
}