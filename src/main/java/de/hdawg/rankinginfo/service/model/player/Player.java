package de.hdawg.rankinginfo.service.model.player;

import de.hdawg.rankinginfo.service.model.Federation;
import de.hdawg.rankinginfo.service.model.Nationality;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Player {
  private final String dtbId;
  private final String firstname;
  private final String lastname;
  private final Nationality nationality;

  private final String currentClub;
  private final Federation currentFederation;
}
