package de.hdawg.rankinginfo.model;

import java.time.LocalDate;

/**
 * model class for a ranking used as basis for the ranking-info application.
 */
public class Ranking implements Comparable<Ranking> {

  private LocalDate rankingPeriod;
  private String dtbId;
  private String lastname;
  private String firstname;
  private String points;
  private Nationality nationality;
  private Federation federation;
  private String club;
  private String ageGroup;
  private Integer position;
  private Boolean yearOfBirthRanking;
  private Boolean overallYouthRanking;
  private Boolean endOfYearRanking;

  public Ranking(LocalDate rankingPeriod, String dtbId, String lastname, String firstname,
      String points,
      Nationality nationality, Federation federation, String club, String ageGroup,
      Integer position,
      Boolean yearOfBirthRanking, Boolean overallYouthRanking,
      Boolean endOfYearRanking) {
    this.rankingPeriod = rankingPeriod;
    this.dtbId = dtbId;
    this.lastname = lastname;
    this.firstname = firstname;
    this.points = points;
    this.nationality = nationality;
    this.federation = federation;
    this.club = club;
    this.ageGroup = ageGroup;
    this.position = position;
    this.yearOfBirthRanking = yearOfBirthRanking;
    this.overallYouthRanking = overallYouthRanking;
    this.endOfYearRanking = endOfYearRanking;
  }

  public LocalDate getRankingPeriod() {
    return rankingPeriod;
  }

  public String getDtbId() {
    return dtbId;
  }

  public String getLastname() {
    return lastname;
  }

  public String getFirstname() {
    return firstname;
  }

  public String getPoints() {
    return points;
  }

  public Nationality getNationality() {
    return nationality;
  }

  public Federation getFederation() {
    return federation;
  }

  public String getClub() {
    return club;
  }

  public String getAgeGroup() {
    return ageGroup;
  }

  public Integer getPosition() {
    return position;
  }

  public Boolean getYearOfBirthRanking() {
    return yearOfBirthRanking;
  }

  public Boolean getOverallYouthRanking() {
    return overallYouthRanking;
  }

  public Boolean getEndOfYearRanking() {
    return endOfYearRanking;
  }

  public void setRankingPeriod(LocalDate rankingPeriod) {
    this.rankingPeriod = rankingPeriod;
  }

  public void setDtbId(String dtbId) {
    this.dtbId = dtbId;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public void setPoints(String points) {
    this.points = points;
  }

  public void setNationality(Nationality nationality) {
    this.nationality = nationality;
  }

  public void setFederation(Federation federation) {
    this.federation = federation;
  }

  public void setClub(String club) {
    this.club = club;
  }

  public void setAgeGroup(String ageGroup) {
    this.ageGroup = ageGroup;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }

  public void setYearOfBirthRanking(Boolean yearOfBirthRanking) {
    this.yearOfBirthRanking = yearOfBirthRanking;
  }

  public void setOverallYouthRanking(Boolean overallYouthRanking) {
    this.overallYouthRanking = overallYouthRanking;
  }

  public void setEndOfYearRanking(Boolean endOfYearRanking) {
    this.endOfYearRanking = endOfYearRanking;
  }

  @Override
  public int compareTo(Ranking otherRanking) {
    return getPosition().compareTo(otherRanking.position);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Ranking)) {
      return false;
    }
    final Ranking other = (Ranking) o;
    final Object thisrankingPeriod = this.getRankingPeriod();
    final Object otherrankingPeriod = other.getRankingPeriod();
    if (thisrankingPeriod == null ? otherrankingPeriod != null :
        !thisrankingPeriod.equals(otherrankingPeriod)) {
      return false;
    }
    final Object thisdtbId = this.getDtbId();
    final Object otherdtbId = other.getDtbId();
    if (thisdtbId == null ? otherdtbId != null : !thisdtbId.equals(otherdtbId)) {
      return false;
    }
    final Object thislastname = this.getLastname();
    final Object otherlastname = other.getLastname();
    if (thislastname == null ? otherlastname != null : !thislastname.equals(otherlastname)) {
      return false;
    }
    final Object thisfirstname = this.getFirstname();
    final Object otherfirstname = other.getFirstname();
    if (thisfirstname == null ? otherfirstname != null : !thisfirstname.equals(otherfirstname)) {
      return false;
    }
    final Object thispoints = this.getPoints();
    final Object otherpoints = other.getPoints();
    if (thispoints == null ? otherpoints != null : !thispoints.equals(otherpoints)) {
      return false;
    }
    final Object thisnationality = this.getNationality();
    final Object othernationality = other.getNationality();
    if (thisnationality == null ? othernationality != null :
        !thisnationality.equals(othernationality)) {
      return false;
    }
    final Object thisfederation = this.getFederation();
    final Object otherfederation = other.getFederation();
    if (thisfederation == null ? otherfederation != null :
        !thisfederation.equals(otherfederation)) {
      return false;
    }
    final Object thisclub = this.getClub();
    final Object otherclub = other.getClub();
    if (thisclub == null ? otherclub != null : !thisclub.equals(otherclub)) {
      return false;
    }
    final Object thisageGroup = this.getAgeGroup();
    final Object otherageGroup = other.getAgeGroup();
    if (thisageGroup == null ? otherageGroup != null : !thisageGroup.equals(otherageGroup)) {
      return false;
    }
    final Object thisposition = this.getPosition();
    final Object otherposition = other.getPosition();
    if (thisposition == null ? otherposition != null : !thisposition.equals(otherposition)) {
      return false;
    }
    final Object thisyearOfBirthRanking = this.getYearOfBirthRanking();
    final Object otheryearOfBirthRanking = other.getYearOfBirthRanking();
    if (thisyearOfBirthRanking == null ? otheryearOfBirthRanking != null :
        !thisyearOfBirthRanking.equals(otheryearOfBirthRanking)) {
      return false;
    }
    final Object thisoverallYouthRanking = this.getOverallYouthRanking();
    final Object otheroverallYouthRanking = other.getOverallYouthRanking();
    if (thisoverallYouthRanking == null ? otheroverallYouthRanking != null :
        !thisoverallYouthRanking.equals(otheroverallYouthRanking)) {
      return false;
    }
    final Object thisendOfYearRanking = this.getEndOfYearRanking();
    final Object otherendOfYearRanking = other.getEndOfYearRanking();
    return thisendOfYearRanking == null ? otherendOfYearRanking == null :
        thisendOfYearRanking.equals(otherendOfYearRanking);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}