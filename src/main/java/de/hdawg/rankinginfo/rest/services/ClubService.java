package de.hdawg.rankinginfo.rest.services;

import de.hdawg.rankinginfo.rest.model.club.ClubSearchResult;
import de.hdawg.rankinginfo.rest.repository.RankingRepository;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Service;

/**
 * Service class providing access to club data and players by club.
 */
@Service
public class ClubService {

  private final RankingRepository repository;

  public ClubService(RankingRepository repository) {
    this.repository = repository;
  }

  /**
   * Query the repository and return a search result object.
   *
   * @param searchTerm search term, may be empty
   * @return search result container for clubs
   */
  public ClubSearchResult findClub(String searchTerm) {
    var result = repository.findClubsBySearchTerm(searchTerm);
    return new ClubSearchResult(result.size(), ZonedDateTime.now(), result);
  }
}
