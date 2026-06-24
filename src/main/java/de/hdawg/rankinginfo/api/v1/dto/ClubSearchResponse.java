package de.hdawg.rankinginfo.api.v1.dto;

import java.util.List;

public record ClubSearchResponse(ClubSearchRequest request, List<ClubSearchItem> data) {
  public ClubSearchResponse {
    data = List.copyOf(data);
  }
}
