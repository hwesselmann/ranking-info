package de.hdawg.rankinginfo.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.hdawg.rankinginfo.model.AgeGroup;
import de.hdawg.rankinginfo.model.Gender;
import de.hdawg.rankinginfo.model.Ranking;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RankingRepositoryTest {

  @Autowired
  private RankingRepository sut;

  @DisplayName("check that reading from the database works")
  @Test
  void testReadingFromAnEmptyDatasource() {
    List<Ranking> result =
        sut.getRankingsForListing(LocalDate.of(2022, 1, 1), AgeGroup.u14, Gender.boys, false, false,
            false);
    assertEquals(0, result.size());
  }

}
