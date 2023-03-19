package de.hdawg.rankinginfo.service.repository;

import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;
import de.hdawg.rankinginfo.service.model.Ranking;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Data access layer for ranking data.
 */
@RequiredArgsConstructor
@Component
public class RankingRepository {

  private final JdbcTemplate jdbcTemplate;

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

  public List<Ranking> getRankingsForPlayer(String dtbId, String lastname, String yob) {
    return Collections.emptyList();
  }

  public List<LocalDate> getAvailableRankingPeriods() {
    return Collections.emptyList();
  }
}
