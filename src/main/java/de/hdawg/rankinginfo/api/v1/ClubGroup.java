package de.hdawg.rankinginfo.api.v1;

import java.util.List;

public record ClubGroup(String group, List<ClubPlayerItem> players) {
  public ClubGroup {
    players = List.copyOf(players);
  }
}
