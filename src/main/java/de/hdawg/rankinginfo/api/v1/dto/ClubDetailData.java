package de.hdawg.rankinginfo.api.v1.dto;

import java.util.List;

public record ClubDetailData(String name, List<ClubGroup> groups) {
  public ClubDetailData {
    groups = List.copyOf(groups);
  }
}
