package de.hdawg.rankinginfo.rest.model.player;

import de.hdawg.rankinginfo.model.Federation;
import de.hdawg.rankinginfo.model.Nationality;

public class PlayerSearchItem {
  private String dtbId;
  private String firstname;
  private String lastname;
  private Federation federation;
  private Nationality nationality;
  private String club;

  public PlayerSearchItem(String dtbId, String firstname, String lastname, Federation federation,
                          Nationality nationality, String club) {
    this.dtbId = dtbId;
    this.firstname = firstname;
    this.lastname = lastname;
    this.federation = federation;
    this.nationality = nationality;
    this.club = club;
  }

  public String getDtbId() {
    return dtbId;
  }

  public void setDtbId(String dtbId) {
    this.dtbId = dtbId;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public Federation getFederation() {
    return federation;
  }

  public void setFederation(Federation federation) {
    this.federation = federation;
  }

  public Nationality getNationality() {
    return nationality;
  }

  public void setNationality(Nationality nationality) {
    this.nationality = nationality;
  }

  public String getClub() {
    return club;
  }

  public void setClub(String club) {
    this.club = club;
  }
}
