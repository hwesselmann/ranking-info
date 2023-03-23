package de.hdawg.rankinginfo.service.model.player;

import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;

import java.time.LocalDate;
import java.util.HashMap;

public class Player {

  public Player(String dtbId, String firstname, String lastname, Nationality nationality, String currentClub, Federation currentFederation) {
    this.dtbId = dtbId;
    this.firstname = firstname;
    this.lastname = lastname;
    this.nationality = nationality;
    this.currentClub = currentClub;
    this.currentFederation = currentFederation;
  }

  private final String dtbId;
  private final String firstname;
  private final String lastname;
  private final Nationality nationality;

  private final String currentClub;
  private final Federation currentFederation;

  private HashMap<LocalDate, String> points;
  private HashMap<LocalDate, HashMap<String, String>> rankingPositions;

  private Trend trend;
}
