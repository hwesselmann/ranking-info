package de.hdawg.rankinginfo.service.model.player;

import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;
import de.hdawg.rankinginfo.service.model.club.Club;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private Map<LocalDate, HashMap<String, String>> rankingPositions;
  private Map<String, Trend> trends;
  private List<Club> clubs;

  public Player(String dtbId, String firstname, String lastname, Nationality nationality, String currentClub, Federation currentFederation) {
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

  public Map<LocalDate, HashMap<String, String>> getRankingPositions() {
    return rankingPositions;
  }

  public void setRankingPositions(Map<LocalDate, HashMap<String, String>> rankingPositions) {
    this.rankingPositions = rankingPositions;
  }

  public Map<String, Trend> getTrends() {
    return trends;
  }

  public void setTrends(Map<String, Trend> trends) {
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
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Player player = (Player) o;
    return dtbId.equals(player.dtbId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dtbId);
  }
}
