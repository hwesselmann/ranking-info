package de.hdawg.rankinginfo.service.model.player;

import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;

@RequiredArgsConstructor
public class Player {
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
