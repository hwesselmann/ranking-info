package de.hdawg.rankinginfo.web.viewmodel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ScoreTimeSeries(String name, Map<String, String> data) {
  public ScoreTimeSeries {
    data = Collections.unmodifiableMap(new LinkedHashMap<>(data));
  }
}
