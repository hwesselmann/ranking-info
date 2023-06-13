package de.hdawg.rankinginfo.service.model.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AvailableRankingPeriods {

  private Map<Integer, List<LocalDate>> rankingperiods;

  public AvailableRankingPeriods() {
  }

  public AvailableRankingPeriods(Map<Integer, List<LocalDate>> rankingperiods) {
    this.rankingperiods = rankingperiods;
  }

  public Map<Integer, List<LocalDate>> getRankingperiods() {
    return rankingperiods;
  }

  public void setRankingperiods(Map<Integer, List<LocalDate>> rankingperiods) {
    this.rankingperiods = rankingperiods;
  }
}
