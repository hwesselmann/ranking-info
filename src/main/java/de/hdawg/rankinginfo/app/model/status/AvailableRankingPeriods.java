package de.hdawg.rankinginfo.app.model.status;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class AvailableRankingPeriods {

  private int count;
  private ZonedDateTime requested;
  private Map<Integer, List<LocalDate>> rankingperiods;

  public AvailableRankingPeriods() {
  }

  public AvailableRankingPeriods(int count, ZonedDateTime requested,
      Map<Integer, List<LocalDate>> rankingperiods) {
    this.count = count;
    this.requested = requested;
    this.rankingperiods = rankingperiods;
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

  public Map<Integer, List<LocalDate>> getRankingperiods() {
    return rankingperiods;
  }

  public void setRankingperiods(Map<Integer, List<LocalDate>> rankingperiods) {
    this.rankingperiods = rankingperiods;
  }
}
