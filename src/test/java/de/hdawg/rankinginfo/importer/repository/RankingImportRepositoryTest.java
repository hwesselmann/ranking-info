package de.hdawg.rankinginfo.importer.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.hdawg.rankinginfo.model.Federation;
import de.hdawg.rankinginfo.model.Nationality;
import de.hdawg.rankinginfo.model.Ranking;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
class RankingImportRepositoryTest {

  @Autowired
  RankingImportRepository sut;

  @DisplayName("store list of rankings into the datastore")
  @Test
  void testStoringRankingList() {
    LocalDate rankingPeriod = LocalDate.of(2022, 10, 1);
    List<Ranking> inRankings = new ArrayList<>();
    inRankings.add(createTestRankings(rankingPeriod, "10800001", Nationality.BEL, "1189,4", 60));
    inRankings.add(createTestRankings(rankingPeriod, "10800002", Nationality.GER, "1189,4", 60));
    inRankings.add(createTestRankings(rankingPeriod, "10800003", Nationality.GER, "1189,4", 110));
    inRankings.add(createTestRankings(rankingPeriod, "10800004", Nationality.GER, "Einst.", 110));
    inRankings.add(createTestRankings(rankingPeriod, "10800005", Nationality.JPN, "1189,4", 125));
    inRankings.add(createTestRankings(rankingPeriod, "10800006", Nationality.CRO, "1189,4", 125));
    inRankings.add(createTestRankings(rankingPeriod, "10800007", Nationality.GER, "1189,4", 126));
    inRankings.add(createTestRankings(rankingPeriod, "10800008", Nationality.GER, "PR", 126));
    inRankings.add(createTestRankings(rankingPeriod, "10800009", Nationality.GER, "1189,4", 400));
    inRankings.add(createTestRankings(rankingPeriod, "10800010", Nationality.GER, "1189,4", 401));

    sut.storeRankings(inRankings);

    assertEquals(10, sut.getEntryCount());
  }

  private Ranking createTestRankings(LocalDate rankingPeriod, String dtbId, Nationality nationality,
      String points, int position) {
    return new Ranking(rankingPeriod, dtbId, "Tennismann", "Max", points, nationality,
        Federation.WTV, "TC Musterstadt", "overall", position, false,
        false, false);
  }
}
