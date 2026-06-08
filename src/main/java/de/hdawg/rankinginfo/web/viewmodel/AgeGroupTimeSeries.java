package de.hdawg.rankinginfo.web.viewmodel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record AgeGroupTimeSeries(String name, Map<String, Integer> data) {
  public AgeGroupTimeSeries {
    data = Collections.unmodifiableMap(new LinkedHashMap<>(data));
  }
}
