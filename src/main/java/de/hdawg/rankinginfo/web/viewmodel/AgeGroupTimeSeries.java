package de.hdawg.rankinginfo.web.viewmodel;

import java.util.Map;

public record AgeGroupTimeSeries(String name, Map<String, Integer> data) {
  public AgeGroupTimeSeries {
    data = Map.copyOf(data);
  }
}
