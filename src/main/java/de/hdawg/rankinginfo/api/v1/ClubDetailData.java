package de.hdawg.rankinginfo.api.v1;

import java.util.List;

public record ClubDetailData(String name, List<ClubGroup> groups) {
  public ClubDetailData {
    groups = List.copyOf(groups);
  }
}
