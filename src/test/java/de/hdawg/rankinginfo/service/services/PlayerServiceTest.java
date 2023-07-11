package de.hdawg.rankinginfo.service.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.hdawg.rankinginfo.service.repository.RankingRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class PlayerServiceTest {

  @Mock
  RankingRepository rankingRepository;

  @DisplayName("eligible ageGroups are computed correctly")
  @Test
  void verifyEligibleAgeGroupsAreComputedCorrectly() {
    PlayerService sut = new PlayerService(rankingRepository);
    assertEquals(List.of("U17", "U18"),
        sut.getEligibleAgeGroups("10600000", LocalDate.of(2023, 4, 1)));
    assertEquals(List.of("U17", "U18"),
        sut.getEligibleAgeGroups("10600000", LocalDate.of(2023, 4, 1)));
    assertEquals(List.of("U11", "U12", "U13", "U14", "U15", "U16", "U17", "U18"),
        sut.getEligibleAgeGroups("11300000", LocalDate.of(2024, 4, 1)));
    assertEquals(List.of("U15", "U16", "U17", "U18"),
        sut.getEligibleAgeGroups("20800000", LocalDate.of(2023, 4, 1)));
  }
}
