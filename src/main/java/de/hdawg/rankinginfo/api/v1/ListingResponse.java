package de.hdawg.rankinginfo.api.v1;

import java.util.List;

public record ListingResponse(ListingRequest request, List<ListingItem> data) {
  public ListingResponse {
    data = List.copyOf(data);
  }
}
