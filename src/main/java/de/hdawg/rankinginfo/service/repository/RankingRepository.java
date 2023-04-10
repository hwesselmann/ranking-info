package de.hdawg.rankinginfo.service.repository;

import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;
import de.hdawg.rankinginfo.service.model.Ranking;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

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
   * @param overallRanking   include all players
   * @param endOfYearRanking end of year ranking
   * @return list of rankings
   */
  public List<Ranking> getRankingsForListing(LocalDate quarter, String ageGroup, String gender, boolean isYobRanking, boolean overallRanking, boolean endOfYearRanking) {
    String genderNumericalIdentifier = "1";
    if ("girls".equals(gender)) {
      genderNumericalIdentifier = "2";
    }

    String sql = "SELECT * FROM ranking WHERE rankingperiod='" + quarter
        + "' AND agegroup='" + ageGroup.toUpperCase()
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
    String sql = "SELECT DISTINCT (DTBID), DTBID, FIRSTNAME, LASTNAME, NATIONALITY, CLUB, FEDERATION, RANKINGPERIOD " +
        "FROM RANKING";
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
      sql += " (dtbid LIKE '1" + yob.substring(2, 4) + "%'" +
          " OR dtbid LIKE '2" + yob.substring(2, 4) + "%')";
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
    final String sql = "SELECT * FROM RANKING WHERE DTBID=? ORDER BY RANKINGPERIOD DESC, AGEGROUP ASC";
    return jdbcTemplate.query(sql, new PreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps) throws SQLException {
        ps.setString(1, dtbId);
      }
    }, (rs, rownum) -> new Ranking(rs.getDate(COLUMN_RANKINGPERIOD).toLocalDate(), rs.getString(COLUMN_DTBID),
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
    return Collections.emptyList();
  }
}
