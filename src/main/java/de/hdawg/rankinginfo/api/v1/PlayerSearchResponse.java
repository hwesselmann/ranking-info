package de.hdawg.rankinginfo.api.v1;

import java.util.List;

public record PlayerSearchResponse(PlayerSearchRequest request, List<PlayerSearchItem> data) {
  public PlayerSearchResponse {
    data = List.copyOf(data);
  }
}
