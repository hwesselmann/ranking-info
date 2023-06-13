package de.hdawg.rankinginfo.service.services;

import de.hdawg.rankinginfo.service.model.status.AvailableRankingPeriods;
import de.hdawg.rankinginfo.service.repository.RankingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service performing data logistics and mapping for the information displayed on the status pages.
 */
@Service
public class StatusService {

  private final RankingRepository rankingRepository;

  public StatusService(RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
  }

  /**
   * Get all available ranking periods from the data source.
   *
   * @return value object
   */
  public AvailableRankingPeriods getAvailableRankingPeriods() {
    List<LocalDate> availablePeriods = rankingRepository.getAvailableRankingPeriods();
    Map<Integer, List<LocalDate>> periods = new HashMap<>();
    availablePeriods.forEach(currItem -> {
      if (!periods.containsKey(currItem.minusDays(1).getYear())) {
        periods.put(currItem.minusDays(1).getYear(), new ArrayList<>());
      }
      periods.get(currItem.minusDays(1).getYear()).add(currItem.minusDays(1));
    });
    return new AvailableRankingPeriods(periods);
  }
}
