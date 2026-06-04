package de.hdawg.rankinginfo.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.repository.ImportHistoryRepository;
import de.hdawg.rankinginfo.repository.RankingRepository;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class WebControllerTestBase {

  @Autowired protected MockMvc mockMvc;

  @Autowired protected RankingRepository rankingRepository;

  @Autowired protected ImportHistoryRepository importHistoryRepository;

  protected final LocalDate q = LocalDate.of(2026, 4, 1);
  protected final LocalDate pq = LocalDate.of(2026, 1, 1);

  @BeforeEach
  protected void seed() {
    var now = LocalDateTime.now();
    rankingRepository.saveAll(
        List.of(
            r(10_001_001, "Mueller", "Hans", "m00", q, 1, "500", false, false, "TC Baden", "BAD", now),
            r(10_002_002, "Schmidt", "Peter", "m00", q, 2, "490", false, false, "TC Other", "WTB", now),
            r(10_001_001, "Mueller", "Hans", "m00", pq, 3, "460", false, false, "TC Baden", "BAD", now),
            r(20_001_001, "Meyer", "Anna", "w00", q, 1, "500", false, false, "TC Baden", "BAD", now),
            r(10_711_111, "Juniormann", "Max", "overall", q, 1, "100", false, false, "TC Baden", "BAD", now),
            r(10_711_112, "Juniormann2", "Max", "U14", q, 1, "100", false, true, "TC Baden", "BAD", now)));
  }

  @AfterEach
  void cleanup() {
    rankingRepository.deleteAll();
    importHistoryRepository.deleteAll();
  }

  protected Ranking r(
      int dtbId,
      String lastname,
      String firstname,
      String ageGroup,
      LocalDate date,
      int pos,
      String score,
      boolean ageGroupRanking,
      boolean yobRanking) {
    return r(
        dtbId, lastname, firstname, ageGroup, date, pos, score,
        ageGroupRanking, yobRanking, "TC Test", "TST", LocalDateTime.now());
  }

  protected Ranking r(
      int dtbId,
      String lastname,
      String firstname,
      String ageGroup,
      LocalDate date,
      int pos,
      String score,
      boolean ageGroupRanking,
      boolean yobRanking,
      String club,
      String federation,
      LocalDateTime now) {
    return new Ranking(
        0L, dtbId, lastname, firstname, "GER", ageGroup, date, pos, score,
        club, federation, ageGroupRanking, yobRanking, false, now, now);
  }
}
