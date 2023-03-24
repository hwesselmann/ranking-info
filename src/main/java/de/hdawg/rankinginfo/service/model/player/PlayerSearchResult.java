package de.hdawg.rankinginfo.service.model.player;

import java.time.LocalDateTime;
import java.util.List;

public class PlayerSearchResult {
  private int count;
  private LocalDateTime requested;
  private List<PlayerSearchItem> items;

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public LocalDateTime getRequested() {
    return requested;
  }

  public void setRequested(LocalDateTime requested) {
    this.requested = requested;
  }

  public List<PlayerSearchItem> getItems() {
    return items;
  }

  public void setItems(List<PlayerSearchItem> items) {
    this.items = items;
  }
}
