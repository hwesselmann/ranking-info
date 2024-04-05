package de.hdawg.rankinginfo.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hdawg.rankinginfo.app.repository.RankingRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class PlayerServiceTest {

  @MockBean
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

  @DisplayName("check that the parameters trigger fetching all players")
  @Test
  void checkFetchAllPlayersIsCalled() {
    PlayerService sut = new PlayerService(rankingRepository);
    when(rankingRepository.findAllPlayers()).thenReturn(Collections.emptyList());
    sut.retrieveRankingsForMapping(null, null, null);
    sut.retrieveRankingsForMapping("", "", "");
    verify(rankingRepository, times(2)).findAllPlayers();
  }

  @DisplayName("check that the parameters trigger fetching players by name")
  @Test
  void checkFetchPlayersByNameIsCalled() {
    PlayerService sut = new PlayerService(rankingRepository);
    when(rankingRepository.findPlayersByName(anyString())).thenReturn(Collections.emptyList());
    sut.retrieveRankingsForMapping(null, "Smith", null);
    sut.retrieveRankingsForMapping("", "Smith", "");
    verify(rankingRepository, times(2)).findPlayersByName(anyString());
  }

  @DisplayName("check that the parameters trigger fetching players by dtbid")
  @Test
  void checkFetchPlayersByDtbIdIsCalled() {
    PlayerService sut = new PlayerService(rankingRepository);
    when(rankingRepository.findPlayersByDtbId(anyString())).thenReturn(Collections.emptyList());
    sut.retrieveRankingsForMapping("12345678", "", null);
    sut.retrieveRankingsForMapping("12345678", null, "");
    verify(rankingRepository, times(2)).findPlayersByDtbId(anyString());
  }

  @DisplayName("check that the parameters trigger fetching players by yob")
  @Test
  void checkFetchPlayersByYobIsCalled() {
    PlayerService sut = new PlayerService(rankingRepository);
    when(rankingRepository.findPlayersByYob(anyString())).thenReturn(Collections.emptyList());
    sut.retrieveRankingsForMapping(null, "", "2012");
    sut.retrieveRankingsForMapping("", null, "2008");
    verify(rankingRepository, times(2)).findPlayersByYob(anyString());
  }

  @DisplayName("check that the parameters trigger fetching players by name and yob")
  @Test
  void checkFetchPlayersByNameAndYobIsCalled() {
    PlayerService sut = new PlayerService(rankingRepository);
    when(rankingRepository.findPlayersByNameAndYob(anyString(), anyString())).thenReturn(
        Collections.emptyList());
    sut.retrieveRankingsForMapping(null, "Weston", "2012");
    sut.retrieveRankingsForMapping("", "Parker", "2008");
    verify(rankingRepository, times(2))
        .findPlayersByNameAndYob(anyString(), anyString());
  }

  @DisplayName("check that the parameters trigger fetching players by name and dtbid")
  @Test
  void checkFetchPlayersByNameAndDtbIdIsCalled() {
    PlayerService sut = new PlayerService(rankingRepository);
    when(rankingRepository.findPlayersByNameAndDtbId(anyString(), anyString())).thenReturn(
        Collections.emptyList());
    sut.retrieveRankingsForMapping("12345678", "Weston", null);
    sut.retrieveRankingsForMapping("87654321", "Parker", "");
    verify(rankingRepository, times(2))
        .findPlayersByNameAndDtbId(anyString(), anyString());
  }

  @DisplayName("check that the parameters trigger fetching players by dtbid and yob")
  @Test
  void checkFetchPlayersByDtbAndYobIdIsCalled() {
    PlayerService sut = new PlayerService(rankingRepository);
    when(rankingRepository.findPlayersByDtbIdAndYob(anyString(), anyString())).thenReturn(
        Collections.emptyList());
    sut.retrieveRankingsForMapping("12345678", "", "2009");
    sut.retrieveRankingsForMapping("87654321", null, "2007");
    verify(rankingRepository, times(2))
        .findPlayersByDtbIdAndYob(anyString(), anyString());
  }

  @DisplayName("check that the parameters trigger fetching players by name, dtbid and yob")
  @Test
  void checkFetchPlayersByDtbIdAndNameAndYobIsCalled() {
    PlayerService sut = new PlayerService(rankingRepository);
    when(rankingRepository.findPlayersByDtbIdAndNameAndYob(anyString(), anyString(), anyString()))
        .thenReturn(Collections.emptyList());
    sut.retrieveRankingsForMapping("12345678", "Weston", "2011");
    sut.retrieveRankingsForMapping("87654321", "Parker", "2013");
    verify(rankingRepository, times(2))
        .findPlayersByDtbIdAndNameAndYob(anyString(), anyString(), anyString());
  }
}
