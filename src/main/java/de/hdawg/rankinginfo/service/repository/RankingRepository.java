package de.hdawg.rankinginfo.service.repository;

import de.hdawg.rankinginfo.service.model.Ranking;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class RankingRepository {
  public List<Ranking> getRankingsForListing(LocalDate quarter, String ageGroup, String gender, boolean isYobRanking, boolean overallRanking, boolean endOfYearRanking) {
    return Collections.emptyList();
  }

  public List<Ranking> getRankingsForPlayer(String dtbId, String lastname, String yob) {
    return Collections.emptyList();
  }
}
