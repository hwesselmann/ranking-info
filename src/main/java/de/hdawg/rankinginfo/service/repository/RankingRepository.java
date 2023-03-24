package de.hdawg.rankinginfo.service.repository;

import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;
import de.hdawg.rankinginfo.service.model.Ranking;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Data access layer for ranking data.
 */
@Component
public class RankingRepository {

  private final JdbcTemplate jdbcTemplate;

  public RankingRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

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
        new Ranking(rs.getDate("rankingperiod").toLocalDate(), rs.getString("dtbid"),
            rs.getString("lastname"), rs.getString("firstname"),
            rs.getString("points"), Nationality.valueOf(rs.getString("nationality")),
            Federation.valueOf(rs.getString("federation")), rs.getString("club"),
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
        "FROM RANKING WHERE DTBID LIKE '" + dtbId + "%'";
    if (name != null && !name.isEmpty()) {
      sql += " OR lastname LIKE '%" + name + "%'";
    }
    if (yob != null && !yob.isEmpty()) {
      sql += " OR dtbid LIKE '1" + yob.substring(2, 3) + "%'" +
          " OR dtbid LIKE '2" + yob.substring(2, 3) + "%'";
    }
    sql += " ORDER BY DTBID, RANKINGPERIOD DESC";
    return jdbcTemplate.query(sql, (rs, rownum) ->
        new Ranking(rs.getDate("rankingperiod").toLocalDate(), rs.getString("dtbid"),
            rs.getString("lastname"), rs.getString("firstname"),
            "", Nationality.valueOf(rs.getString("nationality")),
            Federation.valueOf(rs.getString("federation")), rs.getString("club"),
            "", 0, false, false, false)
    );
  }

  public List<LocalDate> getAvailableRankingPeriods() {
    return Collections.emptyList();
  }
}
