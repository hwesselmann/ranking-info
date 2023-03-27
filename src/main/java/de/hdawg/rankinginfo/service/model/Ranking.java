package de.hdawg.rankinginfo.service.model;

import java.time.LocalDate;

/**
 * model for easy straightforward reading of rankings from the database.
 */
public class Ranking {
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

  public Ranking(LocalDate rankingPeriod, String dtbId, String lastname, String firstname, String points,
                 Nationality nationality, Federation federation, String club, String ageGroup, Integer position,
                 Boolean yearOfBirthRanking, Boolean overallYouthRanking, Boolean endOfYearRanking) {
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

  public void setRankingPeriod(LocalDate rankingPeriod) {
    this.rankingPeriod = rankingPeriod;
  }

  public String getDtbId() {
    return dtbId;
  }

  public void setDtbId(String dtbId) {
    this.dtbId = dtbId;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getPoints() {
    return points;
  }

  public void setPoints(String points) {
    this.points = points;
  }

  public Nationality getNationality() {
    return nationality;
  }

  public void setNationality(Nationality nationality) {
    this.nationality = nationality;
  }

  public Federation getFederation() {
    return federation;
  }

  public void setFederation(Federation federation) {
    this.federation = federation;
  }

  public String getClub() {
    return club;
  }

  public void setClub(String club) {
    this.club = club;
  }

  public String getAgeGroup() {
    return ageGroup;
  }

  public void setAgeGroup(String ageGroup) {
    this.ageGroup = ageGroup;
  }

  public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }

  public Boolean getYearOfBirthRanking() {
    return yearOfBirthRanking;
  }

  public void setYearOfBirthRanking(Boolean yearOfBirthRanking) {
    this.yearOfBirthRanking = yearOfBirthRanking;
  }

  public Boolean getOverallYouthRanking() {
    return overallYouthRanking;
  }

  public void setOverallYouthRanking(Boolean overallYouthRanking) {
    this.overallYouthRanking = overallYouthRanking;
  }

  public Boolean getEndOfYearRanking() {
    return endOfYearRanking;
  }

  public void setEndOfYearRanking(Boolean endOfYearRanking) {
    this.endOfYearRanking = endOfYearRanking;
  }
}
