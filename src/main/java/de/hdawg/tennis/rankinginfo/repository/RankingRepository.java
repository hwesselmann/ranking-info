package de.hdawg.tennis.rankinginfo.repository;

import de.hdawg.tennis.rankinginfo.model.Federation;
import de.hdawg.tennis.rankinginfo.model.Ranking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RankingRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Fetch rankings by applying search criteria if applicable.
     *
     * @param period the period a ranking list is requested for
     * @param gender 'Juniorinnen' or 'Junioren'
     * @param ageGroup age group to fetch rankings for including modifiers
     * @param federation filter for a specific federation
     * @param club filter for clubs
     * @return a list of requested rankings (in the right order)
     */
    public List<Ranking> retrieveRankingListItems(String period, String gender, String ageGroup, Federation federation, String club) {
        String sql = "select * from ranking where period=? and gender=? and agegroup=?";
        if (!federation.equals(Federation.NONE)) {
            sql += " and federation='" + federation + "'";
        }
        if (!club.isEmpty()) {
            sql += " and club LIKE '%" + club.strip() + "%'";
        }
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Ranking ranking = new Ranking();
            ranking.setDtbId(rs.getString("dtbid"));
            ranking.setFirstname(rs.getString("firstname"));
            ranking.setLastname(rs.getString("lastname"));
            ranking.setFederation(Federation.valueOf(rs.getString("federation")));
            ranking.setClub(rs.getString("club"));
            ranking.setNationality(rs.getString("nationality"));
            ranking.setPeriod(rs.getDate("period").toLocalDate());
            ranking.setGender(rs.getString("gender"));
            ranking.setYob(rs.getInt("yob"));
            ranking.setPoints(rs.getString("points"));
            ranking.setRank(rs.getInt("rank"));
            ranking.setIncludeAllPlayers(rs.getBoolean("include_all_players"));
            ranking.setOnlyYoBPlayers(rs.getBoolean("only_yob_players"));
            ranking.setEndOfYearRanking(rs.getBoolean("end_of_year_ranking"));

            return ranking;
        }, period, gender, ageGroup);
    }
}
