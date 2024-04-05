package de.hdawg.rankinginfo.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import de.hdawg.rankinginfo.app.model.status.AvailableRankingPeriods;
import de.hdawg.rankinginfo.app.repository.RankingRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("StatusService tests")
@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

  @InjectMocks
  StatusService sut;
  @Mock
  RankingRepository rankingRepository;

  @DisplayName("assert that available periods are mapped correctly")
  @Test
  void checkIfAvailableRankingPeriodsAreMappedCorrectly() {
    when(rankingRepository.getAvailableRankingPeriods())
        .thenReturn(List.of(LocalDate.of(2019, 1, 1),
            LocalDate.of(2019, 4, 1),
            LocalDate.of(2019, 7, 1),
            LocalDate.of(2019, 10, 1),
            LocalDate.of(2020, 1, 1)));
    AvailableRankingPeriods result = sut.getAvailableRankingPeriods();

    assertEquals(2, result.getRankingperiods().keySet().size());
    assertTrue(result.getRankingperiods().containsKey(2018));
    assertEquals(12, result.getRankingperiods().get(2019).get(3).getMonthValue());
    assertEquals(4, result.getRankingperiods().get(2019).size());
  }
}
