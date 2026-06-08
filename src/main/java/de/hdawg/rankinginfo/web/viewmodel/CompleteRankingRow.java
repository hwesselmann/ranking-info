package de.hdawg.rankinginfo.web.viewmodel;

import java.util.Map;

public record CompleteRankingRow(String date, String score, Map<String, Integer> ageGroupPositions) {
  public CompleteRankingRow {
    ageGroupPositions = Map.copyOf(ageGroupPositions);
  }
}
