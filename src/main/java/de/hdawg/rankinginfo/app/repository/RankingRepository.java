package de.hdawg.rankinginfo.app.repository;

import de.hdawg.rankinginfo.app.model.club.Club;
import de.hdawg.rankinginfo.model.AgeGroup;
import de.hdawg.rankinginfo.model.Federation;
import de.hdawg.rankinginfo.model.Gender;
import de.hdawg.rankinginfo.model.Nationality;
import de.hdawg.rankinginfo.model.Ranking;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data access layer for ranking data.
 */
@Repository
@Transactional(readOnly = true)
public class RankingRepository {

  public static final String COLUMN_RANKINGPERIOD = "rankingperiod";
  public static final String COLUMN_DTBID = "dtbid";
  public static final String COLUMN_LASTNAME = "lastname";
  public static final String COLUMN_FIRSTNAME = "firstname";
  public static final String COLUMN_POINTS = "points";
  public static final String COLUMN_NATIONALITY = "nationality";
  public static final String COLUMN_FEDERATION = "federation";
  public static final String COLUMN_CLUB = "club";
  private final JdbcClient jdbcClient;

  public RankingRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
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
    var genderNumericalIdentifier = Gender.girls == gender ? "2%" : "1%";

    var sql = "select * from ranking where rankingperiod = :quarter and agegroup = :age_group and "
        + "dtbid LIKE :dtb_id and yobrankings = :yobranking and overallranking = :overallranking "
        + " and endofyearranking = :endofyearranking order by rankingposition asc, points asc";
    return jdbcClient.sql(sql)
        .param("quarter", quarter)
        .param("age_group", ageGroup.name().toUpperCase())
        .param("dtb_id", genderNumericalIdentifier)
        .param("yobranking", isYobRanking)
        .param("overallranking", overallRanking)
        .param("endofyearranking", endOfYearRanking)
        .query(new RankingMapper())
        .list();
  }

  /**
   * find players by id, lastname and yob.
   *
   * @param dtbId dtbid
   * @param name  lastname
   * @param yob   yob
   * @return list of players for the given params
   */
  public List<Ranking> findPlayersByDtbIdAndNameAndYob(final String dtbId, final String name,
      final String yob) {
    var sql = "select distinct (dtbid), dtbid, firstname, lastname, nationality, club, federation, "
        + "rankingperiod from ranking where lastname like :lastname and dtbid like :dtb_id "
        + "and (dtbid like :boysId or dtbid like :girlsId)";
    return jdbcClient.sql(sql)
        .param("dtb_id", dtbId + "%")
        .param("lastname", "%" + name + "%")
        .param("boysId", "1" + yob.substring(2, 4) + "%")
        .param("girlsId", "2" + yob.substring(2, 4) + "%")
        .query(new RankingPlayerMapper()).list();
  }

  /**
   * find players by lastname and yob.
   *
   * @param name lastname
   * @param yob  yob
   * @return list of players for the given params
   */
  public List<Ranking> findPlayersByNameAndYob(final String name, final String yob) {
    var sql = "select distinct (dtbid), dtbid, firstname, lastname, nationality, club, federation, "
        + "rankingperiod from ranking where lastname like :lastname and (dtbid like :boysId "
        + "or dtbid like :girlsId)";
    return jdbcClient.sql(sql)
        .param("lastname", "%" + name + "%")
        .param("boysId", "1" + yob.substring(2, 4) + "%")
        .param("girlsId", "2" + yob.substring(2, 4) + "%")
        .query(new RankingPlayerMapper()).list();
  }

  /**
   * find players by id and lastname.
   *
   * @param dtbId dtbid
   * @param name  lastname
   * @return list of players for the given params
   */
  public List<Ranking> findPlayersByNameAndDtbId(final String dtbId, final String name) {
    var sql = "select distinct (dtbid), dtbid, firstname, lastname, nationality, club, federation, "
        + "rankingperiod from ranking where lastname like :lastname and dtbid like :dtb_id";
    return jdbcClient.sql(sql)
        .param("dtb_id", dtbId + "%")
        .param("lastname", "%" + name + "%")
        .query(new RankingPlayerMapper()).list();
  }

  /**
   * find players by lastname.
   *
   * @param name lastname
   * @return list of players for the given params
   */
  public List<Ranking> findPlayersByName(final String name) {
    var sql = "select distinct (dtbid), dtbid, firstname, lastname, nationality, club, federation, "
        + "rankingperiod from ranking where lastname like :lastname";
    return jdbcClient.sql(sql)
        .param("lastname", "%" + name + "%")
        .query(new RankingPlayerMapper()).list();
  }

  /**
   * find players by id and yob.
   *
   * @param dtbId dtbid
   * @param yob   yob
   * @return list of players for the given params
   */
  public List<Ranking> findPlayersByDtbIdAndYob(final String dtbId, final String yob) {
    var sql = "select distinct (dtbid), dtbid, firstname, lastname, nationality, club, federation, "
        + "rankingperiod from ranking where dtbid like :dtb_id and (dtbid like :boysId "
        + "or dtbid like :girlsId)";
    return jdbcClient.sql(sql)
        .param("dtb_id", dtbId + "%")
        .param("boysId", "1" + yob.substring(2, 4) + "%")
        .param("girlsId", "2" + yob.substring(2, 4) + "%")
        .query(new RankingPlayerMapper()).list();
  }

  /**
   * find players by yob.
   *
   * @param yob yob
   * @return list of players for the given params
   */
  public List<Ranking> findPlayersByYob(final String yob) {
    var sql = "select distinct (dtbid), dtbid, firstname, lastname, nationality, club, federation, "
        + "rankingperiod from ranking where (dtbid like :boysId or dtbid like :girlsId)";
    return jdbcClient.sql(sql)
        .param("boysId", "1" + yob.substring(2, 4) + "%")
        .param("girlsId", "2" + yob.substring(2, 4) + "%")
        .query(new RankingPlayerMapper()).list();
  }

  /**
   * find players by id.
   *
   * @param dtbId dtbid
   * @return list of players for the given params
   */
  public List<Ranking> findPlayersByDtbId(final String dtbId) {
    var sql = "select distinct (dtbid), dtbid, firstname, lastname, nationality, club, federation, "
        + "rankingperiod from ranking where dtbid like :dtb_id";
    return jdbcClient.sql(sql)
        .param("dtb_id", dtbId + "%")
        .query(new RankingPlayerMapper()).list();
  }

  /**
   * fetch unique rankings for every dtb id in the database.
   *
   * @return list of players
   */
  public List<Ranking> findAllPlayers() {
    var sql = "select distinct (dtbid), dtbid, firstname, lastname, nationality, club, federation, "
        + "rankingperiod from ranking";
    return jdbcClient.sql(sql).query(new RankingPlayerMapper()).list();
  }

  /**
   * get all rankings for a player with a given dtb id.
   *
   * @param dtbId unique id
   * @return list of rankings
   */
  public List<Ranking> getRankingsForPlayer(final String dtbId) {
    var sql =
        "select * from ranking where dtbid = :dtb_id order by rankingperiod desc, agegroup asc";
    return jdbcClient.sql(sql).param("dtb_id", dtbId).query(new RankingMapper()).list();
  }

  /**
   * retrieve all available ranking periods.
   *
   * @return list of ranking periods
   */
  public List<LocalDate> getAvailableRankingPeriods() {
    var sql = "select distinct(rankingperiod) from ranking order by rankingperiod asc";
    return jdbcClient.sql(sql).query(LocalDate.class).list();
  }

  /**
   * Retrieve the most recent ranking period in the system.
   *
   * @return ranking period or null
   */
  public LocalDate getMostRecentRankingPeriod() {
    return getAvailableRankingPeriods().stream().max(Comparator.naturalOrder()).orElse(null);
  }

  /**
   * find all clubs matching a given search term.
   *
   * @param searchTerm club name or club name part
   * @return list of matches
   */
  public List<Club> findClubsBySearchTerm(String searchTerm) {
    var sql = "select distinct(club), federation from ranking where upper(club) like upper(:club)";
    return jdbcClient.sql(sql).param("club", "%" + searchTerm + "%").query(new ClubMapper()).list();
  }

  /**
   * find all rankings for unique players with a ranking on the most recent listing for a given
   * club.
   *
   * @param name                    club name
   * @param mostRecentRankingPeriod most recent ranking period
   * @return list of matched rankings
   */
  public List<Ranking> findRankingsForPlayersOfGivenClub(String name,
      LocalDate mostRecentRankingPeriod) {
    var sql = "select * from ranking where club=:club and ranking_period = :ranking_period "
        + "and agegroup='U18' and overallranking=true";
    return jdbcClient.sql(sql)
        .param("club", name)
        .param("ranking_period", mostRecentRankingPeriod)
        .query(new RankingMapper())
        .list();
  }

  private static class RankingMapper implements RowMapper<Ranking> {

    @Override
    public Ranking mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new Ranking(
          rs.getDate(COLUMN_RANKINGPERIOD).toLocalDate(),
          rs.getString(COLUMN_DTBID),
          rs.getString(COLUMN_LASTNAME),
          rs.getString(COLUMN_FIRSTNAME),
          rs.getString(COLUMN_POINTS),
          Nationality.valueOf(rs.getString(COLUMN_NATIONALITY)),
          Federation.valueOf(rs.getString(COLUMN_FEDERATION)),
          rs.getString(COLUMN_CLUB),
          rs.getString("agegroup"),
          rs.getInt("rankingposition"),
          rs.getBoolean("yobrankings"),
          rs.getBoolean("overallranking"),
          rs.getBoolean("endofyearranking"));
    }
  }

  private static class RankingPlayerMapper implements RowMapper<Ranking> {

    @Override
    public Ranking mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new Ranking(
          rs.getDate(COLUMN_RANKINGPERIOD).toLocalDate(),
          rs.getString(COLUMN_DTBID),
          rs.getString(COLUMN_LASTNAME),
          rs.getString(COLUMN_FIRSTNAME),
          "",
          Nationality.valueOf(rs.getString(COLUMN_NATIONALITY)),
          Federation.valueOf(rs.getString(COLUMN_FEDERATION)),
          rs.getString(COLUMN_CLUB),
          "overall",
          0,
          false,
          false,
          false);
    }
  }

  private static class ClubMapper implements RowMapper<Club> {

    @Override
    public Club mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new Club(
          rs.getString(COLUMN_CLUB),
          Federation.valueOf(rs.getString(COLUMN_FEDERATION))
      );
    }
  }
}
