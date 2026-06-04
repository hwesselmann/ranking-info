package de.hdawg.rankinginfo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.hdawg.rankinginfo.domain.Ranking;
import de.hdawg.rankinginfo.repository.RankingRepository;

@SpringBootTest
class PlayerServiceTest {

  @Autowired PlayerService playerService;

  @Autowired RankingRepository rankingRepository;

  private final int maleId = 10_001_001;
  private final int femaleId = 20_001_001;

  @BeforeEach
  void seed() {
    var now = LocalDateTime.now();
    var q = LocalDate.of(2026, 4, 1);
    rankingRepository.saveAll(
        List.of(
            r(maleId, "Mueller", "Hans", "m00", q, 1, now),
            r(femaleId, "Meyer", "Anna", "w00", q, 1, now),
            r(maleId, "Mueller", "Hans", "overall", q, 2, now)));
  }

  @AfterEach
  void cleanup() {
    rankingRepository.deleteAll();
  }

  @Test
  @DisplayName("loadPlayerProfile returns correct player")
  void loadPlayerProfileReturnsCorrectPlayer() {
    var player = playerService.loadPlayerProfile(maleId);
    assertEquals("Mueller", player.lastname());
    assertEquals(maleId, player.dtbId());
  }

  @Test
  @DisplayName("loadPlayerProfile deduplicates clubs")
  void loadPlayerProfileDeduplicatesClubs() {
    var player = playerService.loadPlayerProfile(maleId);
    assertEquals(1, player.clubs().size());
  }

  @Test
  @DisplayName("findPlayersByLastname returns matching players")
  void findPlayersByLastnameReturnsMatchingPlayers() {
    var results = playerService.findPlayersByLastname("Mueller");
    assertEquals(1, results.size());
    assertEquals("Mueller", results.get(0).lastname());
  }

  @Test
  @DisplayName("findPlayersByLastname is case-insensitive")
  void findPlayersByLastnameIsCaseInsensitive() {
    var results = playerService.findPlayersByLastname("mueller");
    assertEquals(1, results.size());
  }

  @Test
  @DisplayName("findPlayersByLastname deduplicates by dtb_id")
  void findPlayersByLastnameDeduplicatesByDtbId() {
    var results = playerService.findPlayersByLastname("Mueller");
    assertEquals(1, results.size());
  }

  @Test
  @DisplayName("findPlayersByLastnameAndYob filters by year of birth")
  void findPlayersByLastnameAndYobFiltersByYearOfBirth() {
    var results = playerService.findPlayersByLastnameAndYob("Mueller", 100, 200);
    assertEquals(1, results.size());
  }

  @Test
  @DisplayName("findPlayersByLastnameAndYob returns empty for wrong yob")
  void findPlayersByLastnameAndYobReturnsEmptyForWrongYob() {
    var results = playerService.findPlayersByLastnameAndYob("Mueller", 999, 1000);
    assertTrue(results.isEmpty());
  }

  @Test
  @DisplayName("findPlayersByYob returns players in yob range")
  void findPlayersByYobReturnsPlayersInYobRange() {
    var results = playerService.findPlayersByYob(100, 200);
    assertTrue(results.isEmpty() == false);
  }

  @Test
  @DisplayName("findPlayersByDtbIdRange returns player for exact id")
  void findPlayersByDtbIdRangeReturnsPlayerForExactId() {
    var results = playerService.findPlayersByDtbIdRange(maleId, maleId);
    assertEquals(1, results.size());
    assertEquals(maleId, results.get(0).dtbId());
  }

  @Test
  @DisplayName("findPlayersByDtbIdRange returns empty for out-of-range")
  void findPlayersByDtbIdRangeReturnsEmptyForOutOfRange() {
    var results = playerService.findPlayersByDtbIdRange(99_000_000, 99_999_999);
    assertTrue(results.isEmpty());
  }

  private Ranking r(
      int dtbId,
      String lastname,
      String firstname,
      String ageGroup,
      LocalDate date,
      int pos,
      LocalDateTime now) {
    return new Ranking(
        0L, dtbId, lastname, firstname, "GER", ageGroup, date, pos, "100",
        "TC Test", "TST", false, false, false, now, now);
  }
}
