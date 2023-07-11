package de.hdawg.rankinginfo.service.model.player;

import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;
import de.hdawg.rankinginfo.service.model.club.Club;
import java.util.List;
import java.util.Objects;

/**
 * domain object for player data.
 */
public class Player {

  private final String dtbId;
  private final String firstname;
  private final String lastname;
  private final Nationality nationality;
  private final String currentClub;
  private final Federation currentFederation;
  private List<PointsHistory> points;
  private List<RankingItem> officialRankingPositions;
  private List<RankingItem> yobRankingPositions;
  private List<RankingItem> overallRankingPositions;
  private List<RankingItem> endOfYearRankingPositions;
  private List<Trend> trends;
  private List<Club> clubs;

  public Player(String dtbId, String firstname, String lastname, Nationality nationality,
                String currentClub, Federation currentFederation) {
    this.dtbId = dtbId;
    this.firstname = firstname;
    this.lastname = lastname;
    this.nationality = nationality;
    this.currentClub = currentClub;
    this.currentFederation = currentFederation;
  }

  public String getDtbId() {
    return dtbId;
  }

  public String getFirstname() {
    return firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public Nationality getNationality() {
    return nationality;
  }

  public String getCurrentClub() {
    return currentClub;
  }

  public Federation getCurrentFederation() {
    return currentFederation;
  }

  public List<PointsHistory> getPoints() {
    return points;
  }

  public void setPoints(List<PointsHistory> points) {
    this.points = points;
  }

  public List<RankingItem> getOfficialRankingPositions() {
    return officialRankingPositions;
  }

  public void setOfficialRankingPositions(List<RankingItem> rankingPositions) {
    this.officialRankingPositions = rankingPositions;
  }

  public List<RankingItem> getYobRankingPositions() {
    return yobRankingPositions;
  }

  public void setYobRankingPositions(List<RankingItem> rankingPositions) {
    this.yobRankingPositions = rankingPositions;
  }

  public List<RankingItem> getOverallRankingPositions() {
    return overallRankingPositions;
  }

  public void setOverallRankingPositions(List<RankingItem> rankingPositions) {
    this.overallRankingPositions = rankingPositions;
  }

  public List<RankingItem> getEndOfYearRankingPositions() {
    return endOfYearRankingPositions;
  }

  public void setEndOfYearRankingPositions(List<RankingItem> rankingPositions) {
    this.endOfYearRankingPositions = rankingPositions;
  }

  public List<Trend> getTrends() {
    return trends;
  }

  public void setTrends(List<Trend> trends) {
    this.trends = trends;
  }

  public List<Club> getClubs() {
    return clubs;
  }

  public void setClubs(List<Club> clubs) {
    this.clubs = clubs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Player player = (Player) o;
    return dtbId.equals(player.dtbId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dtbId);
  }
}
