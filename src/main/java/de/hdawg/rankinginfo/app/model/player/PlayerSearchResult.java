package de.hdawg.rankinginfo.app.model.player;

import java.time.ZonedDateTime;
import java.util.List;

public class PlayerSearchResult {

  private int count;
  private ZonedDateTime requested;
  private List<PlayerSearchItem> items;

  public PlayerSearchResult(int count, ZonedDateTime requested, List<PlayerSearchItem> items) {
    this.count = count;
    this.requested = requested;
    this.items = items;
  }

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

  public List<PlayerSearchItem> getItems() {
    return items;
  }

  public void setItems(List<PlayerSearchItem> items) {
    this.items = items;
  }
}
