package de.hdawg.rankinginfo.app.model.club;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Model for a club search result.
 */
public class ClubSearchResult {

  private int count;
  private ZonedDateTime requested;
  private List<Club> items;

  public ClubSearchResult(int count, ZonedDateTime requested, List<Club> items) {
    this.count = count;
    this.requested = requested;
    this.items = items;
  }

  public int getCount() {
    return count;
  }

  public ZonedDateTime getRequested() {
    return requested;
  }

  public List<Club> getItems() {
    return items;
  }
}
