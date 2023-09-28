package de.hdawg.rankinginfo.rest.model.club;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Object model for club player listing.
 */
public class ClubPlayerResult {

  private String name;
  private int count;
  private ZonedDateTime requested;
  private List<ClubPlayer> items;

  public ClubPlayerResult(String name, int count, ZonedDateTime requested, List<ClubPlayer> items) {
    this.name = name;
    this.count = count;
    this.requested = requested;
    this.items = items;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public List<ClubPlayer> getItems() {
    return items;
  }

  public void setItems(List<ClubPlayer> items) {
    this.items = items;
  }
}
