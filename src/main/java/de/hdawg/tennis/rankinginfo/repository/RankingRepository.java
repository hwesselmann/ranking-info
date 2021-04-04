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
    public List<Ranking> retrieveRankingListItems(String period, boolean gender, String ageGroup, Federation federation, String club) {
        // TODO also respect federation and club params in the query if set
        // TODO map ageGroup to query (select & order)
        String sql = "select * from ranking where period=? and gender=?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Ranking ranking = new Ranking();
            ranking.setDtbId(rs.getString("dtbid"));
            ranking.setFirstname(rs.getString("firstname"));
            ranking.setLastname(rs.getString("lastname"));
            ranking.setFederation(Federation.valueOf(rs.getString("federation")));
            ranking.setClub(rs.getString("club"));
            ranking.setNationality(rs.getString("nationality"));
            ranking.setPeriod(rs.getDate("period").toLocalDate());
            ranking.setGender(rs.getBoolean("gender"));
            ranking.setYob(rs.getInt("yob"));
            ranking.setPoints(rs.getString("points"));
            ranking.setRankOverall(rs.getInt("rankOverall"));
            ranking.setRankU18(rs.getInt("rankU18"));
            ranking.setRankU18c(rs.getInt("rankU18c"));
            ranking.setRankU18y(rs.getInt("rankU18y"));
            ranking.setRankU17c(rs.getInt("rankU17c"));
            ranking.setRankU17y(rs.getInt("rankU17y"));
            ranking.setRankU16(rs.getInt("rankU16"));
            ranking.setRankU16c(rs.getInt("rankU16c"));
            ranking.setRankU16y(rs.getInt("rankU16y"));
            ranking.setRankU15c(rs.getInt("rankU15c"));
            ranking.setRankU15y(rs.getInt("rankU15y"));
            ranking.setRankU14(rs.getInt("rankU14"));
            ranking.setRankU14c(rs.getInt("rankU14c"));
            ranking.setRankU14y(rs.getInt("rankU14y"));
            ranking.setRankU13c(rs.getInt("rankU13c"));
            ranking.setRankU13y(rs.getInt("rankU13y"));
            ranking.setRankU12(rs.getInt("rankU12"));
            ranking.setRankU12c(rs.getInt("rankU12c"));
            ranking.setRankU12y(rs.getInt("rankU12y"));
            ranking.setRankU11y(rs.getInt("rankU11y"));

            return ranking;
        }, period, gender);
    }
}
