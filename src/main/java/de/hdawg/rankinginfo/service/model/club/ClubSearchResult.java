package de.hdawg.rankinginfo.service.model.club;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Model for a club search result.
 */
public class ClubSearchResult {
  private int count;
  private ZonedDateTime requested;
  private List<Club> items;

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public ZonedDateTime getRequested() {
    return requested;
  }

  public void setRequested(ZonedDateTime requested) {
    this.requested = requested;
  }

  public List<Club> getItems() {
    return items;
  }

  public void setItems(List<Club> items) {
    this.items = items;
  }
}
